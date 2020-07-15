package it.course.myblog.controller;

import java.time.Instant;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import it.course.myblog.entity.Post;
import it.course.myblog.entity.Report;
import it.course.myblog.entity.ReportCompositeKey;
import it.course.myblog.entity.User;
import it.course.myblog.entity.UserReported;
import it.course.myblog.entity.UserReportedId;
import it.course.myblog.payload.request.ReportNoteRequest;
import it.course.myblog.payload.request.ReportRequest;
import it.course.myblog.payload.response.ApiResponseCustom;
import it.course.myblog.payload.response.CountReportsByPostResponse;
import it.course.myblog.payload.response.CountReportsByReporterResponse;
import it.course.myblog.payload.response.ReportDetailResponse;
import it.course.myblog.repository.PostRepository;
import it.course.myblog.repository.ReportRepository;
import it.course.myblog.repository.UserReportedRepository;
import it.course.myblog.repository.UserRepository;
import it.course.myblog.security.JwtUser;
import it.course.myblog.service.UserService;

@RestController
public class ReportController {
	
	@Autowired
	PostRepository postRepository;
	
	@Autowired
	UserRepository userRepository;
	
	@Autowired
	ReportRepository reportRepository;
	
	@Autowired
	UserService userService;
	
	@Autowired
	UserReportedRepository userReportedRepository;
	
	@PostMapping("private/create-report")
	@PreAuthorize("hasRole('READER')")
	public ResponseEntity<ApiResponseCustom> createReport(@Valid @RequestBody ReportRequest reportRequest, HttpServletRequest request){
		
		Optional<Post> p = postRepository.findByIdAndVisibleTrue(reportRequest.getPostId());
		if(!p.isPresent())
			return new ResponseEntity<ApiResponseCustom>(new ApiResponseCustom (
					Instant.now(), 200, "OK", "Post not found", request.getRequestURI()), HttpStatus.OK);
		
		
		JwtUser jwtUser = userService.getAuthenticatedUser();
		Optional<User> reporter = userRepository.findByUsername(jwtUser.getUsername());
		
		Optional<Report> reportToFind = reportRepository.findById(new ReportCompositeKey(reporter.get(), p.get()));
		if(reportToFind.isPresent())
			return new ResponseEntity<ApiResponseCustom>(new ApiResponseCustom (
					Instant.now(), 200, "OK", "The post has been reported by you on "+reportToFind.get().getCreatedAt(), request.getRequestURI()), HttpStatus.OK);
		
		Report r = new Report();
		r.setReportCompositeKey(new ReportCompositeKey(reporter.get(), p.get()));
		r.setReportReason(reportRequest.getReportReason());
		
		reportRepository.save(r);
		
		return new ResponseEntity<ApiResponseCustom>(new ApiResponseCustom (
				Instant.now(), 200, "OK", "Report successfully created", request.getRequestURI()), HttpStatus.OK);
		
		
	}
	
	@PutMapping("private/update-report")
	@PreAuthorize("hasRole('ADMIN')")
	@Transactional
	public ResponseEntity<ApiResponseCustom> updateReport(@Valid @RequestBody ReportNoteRequest reportNoteRequest, HttpServletRequest request){
		
		Optional<Post> p = postRepository.findByIdAndVisibleTrue(reportNoteRequest.getPostId());
		if(!p.isPresent())
			return new ResponseEntity<ApiResponseCustom>(new ApiResponseCustom (
					Instant.now(), 200, "OK", "Post not found", request.getRequestURI()), HttpStatus.OK);
		
		Optional<User> u = userRepository.findById(reportNoteRequest.getReporterId());
		if(!u.isPresent())
			return new ResponseEntity<ApiResponseCustom>(new ApiResponseCustom (
					Instant.now(), 200, "OK", "User not found", request.getRequestURI()), HttpStatus.OK);
		
		Optional<Report> reportToFind = reportRepository.findById(new ReportCompositeKey(u.get(), p.get()));
		if(!reportToFind.isPresent())
			return new ResponseEntity<ApiResponseCustom>(new ApiResponseCustom (
					Instant.now(), 200, "OK", "Report not found", request.getRequestURI()), HttpStatus.OK);
		
		reportToFind.get().setReportNote(reportNoteRequest.getReportNote());
		
		reportRepository.save(reportToFind.get());
		
		if(reportNoteRequest.isValid()) {
			
			// set not visible the post
			p.get().setVisible(false);
			p.get().setApproved(false);
			postRepository.save(p.get());
			
			// ctrl if the author must be banned
			Optional<UserReported> ur = userReportedRepository.findByUserReportedId(new UserReportedId(p.get().getAuthor()));
			if(ur.isPresent()) {
				if(ur.get().getReportNumber() < 2) {
					ur.get().setReportNumber(ur.get().getReportNumber() + 1);
					userReportedRepository.save(ur.get());
				} else {
					Optional<User> user = userRepository.findById(p.get().getAuthor().getId());
					user.get().setEnabled(false);
					userRepository.save(user.get());
				}
				
			} else {
				userReportedRepository.save(new UserReported(new UserReportedId(p.get().getAuthor()),1));
			}
		}
		
		return new ResponseEntity<ApiResponseCustom>(new ApiResponseCustom (
				Instant.now(), 200, "OK", "Report successfully update", request.getRequestURI()), HttpStatus.OK);
		
	}
	
	@GetMapping("private/get-reports-not-checked")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<ApiResponseCustom> getReportsNotChecked(HttpServletRequest request) {
		
		List<Report> reports = reportRepository.findByReportNoteIsNullOrderByCreatedAtAsc();
		if (reports.isEmpty())
			return new ResponseEntity<ApiResponseCustom>(new ApiResponseCustom(Instant.now(), 200, "OK",
					"No reports waiting for check", request.getRequestURI()), HttpStatus.OK);

		List<ReportDetailResponse> rpsdet = reports.stream().map(ReportDetailResponse::createFromReport).collect(Collectors.toList());
		return new ResponseEntity<ApiResponseCustom>(
				new ApiResponseCustom(Instant.now(), 200, "OK", rpsdet, request.getRequestURI()), HttpStatus.OK);

	}
	
	@GetMapping("private/get-reports-by-reporter")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<ApiResponseCustom> getReportsByReporter(@RequestParam String reporterUsername, HttpServletRequest request){
		
		Optional<User> u = userRepository.findByUsername(reporterUsername);
		if(!u.isPresent())
			return new ResponseEntity<ApiResponseCustom>(new ApiResponseCustom (
					Instant.now(), 200, "OK", "User not found", request.getRequestURI()), HttpStatus.OK);
		
		List<Report> reports = reportRepository.findByReportCompositeKeyReporter(u.get());
		if (reports.isEmpty())
			return new ResponseEntity<ApiResponseCustom>(new ApiResponseCustom(Instant.now(), 200, "OK",
					"No reports found for reporter : "+reporterUsername, request.getRequestURI()), HttpStatus.OK);
		
		List<ReportDetailResponse> rpsdet = reports.stream().map(ReportDetailResponse::createFromReport).collect(Collectors.toList());
		return new ResponseEntity<ApiResponseCustom>(
				new ApiResponseCustom(Instant.now(), 200, "OK", rpsdet, request.getRequestURI()), HttpStatus.OK);

	}
	
	@GetMapping("private/count-reports-by-post")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<ApiResponseCustom> countReportsByPost(@RequestParam Long id, HttpServletRequest request){
		
		Optional<Post> p = postRepository.findById(id);
		if(!p.isPresent())
			return new ResponseEntity<ApiResponseCustom>(new ApiResponseCustom (
					Instant.now(), 200, "OK", "Post not found", request.getRequestURI()), HttpStatus.OK);
		
		
		CountReportsByPostResponse cr = new CountReportsByPostResponse(
				p.get().getId(),
				p.get().getTitle(),
				reportRepository.countByReportCompositeKeyReportedPost(p.get())
				);		
		
		return new ResponseEntity<ApiResponseCustom>(
				new ApiResponseCustom(Instant.now(), 200, "OK", cr, request.getRequestURI()), HttpStatus.OK);
		
	}
		

	@GetMapping("private/count-reports-group-by-posts")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<ApiResponseCustom> countReportsGroupByPosts(HttpServletRequest request){
		
		List<Report> reports = reportRepository.findAll();
		if (reports.isEmpty())
			return new ResponseEntity<ApiResponseCustom>(new ApiResponseCustom(Instant.now(), 200, "OK",
					"No reports found", request.getRequestURI()), HttpStatus.OK);
		
		Set<Post> posts = reports.stream()
				.map(p -> p.getReportCompositeKey().getReportedPost())
				.collect(Collectors.toSet());
		
		List<CountReportsByPostResponse> crs = posts.stream().map(p ->new CountReportsByPostResponse(
				p.getId(),
				p.getTitle(),
				reportRepository.countByReportCompositeKeyReportedPost(p)
				)).collect(Collectors.toList());
		
		return new ResponseEntity<ApiResponseCustom>(
				new ApiResponseCustom(Instant.now(), 200, "OK", crs, request.getRequestURI()), HttpStatus.OK);
		
	}
	
	@GetMapping("private/count-reports-group-by-reporter")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<ApiResponseCustom> countReportsGroupByReporter(HttpServletRequest request){
		
		List<Report> reports = reportRepository.findAll();
		if (reports.isEmpty())
			return new ResponseEntity<ApiResponseCustom>(new ApiResponseCustom(Instant.now(), 200, "OK",
					"No reports found", request.getRequestURI()), HttpStatus.OK);
		
		Set<User> users = reports.stream()
				.map(p -> p.getReportCompositeKey().getReporter())
				.collect(Collectors.toSet());
		
		List<CountReportsByReporterResponse> crs = users.stream().map(u ->new CountReportsByReporterResponse(
				u.getId(),
				u.getUsername(),
				reportRepository.countByReportCompositeKeyReporter(u)
				)).collect(Collectors.toList());
		
		return new ResponseEntity<ApiResponseCustom>(
				new ApiResponseCustom(Instant.now(), 200, "OK", crs, request.getRequestURI()), HttpStatus.OK);
		
	}
}
