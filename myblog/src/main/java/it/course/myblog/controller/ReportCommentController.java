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

import it.course.myblog.entity.Comment;
import it.course.myblog.entity.ReportComment;
import it.course.myblog.entity.ReportCommentCompositeKey;
import it.course.myblog.entity.User;
import it.course.myblog.entity.UserReported;
import it.course.myblog.entity.UserReportedId;
import it.course.myblog.payload.request.ReportCommentNoteRequest;
import it.course.myblog.payload.request.ReportCommentRequest;
import it.course.myblog.payload.response.ApiResponseCustom;
import it.course.myblog.payload.response.CountReportsByReporterResponse;
import it.course.myblog.payload.response.CountReportsCommentByCommentResponse;
import it.course.myblog.payload.response.ReportCommentDetailResponse;
import it.course.myblog.repository.CommentRepository;
import it.course.myblog.repository.ReportCommentRepository;
import it.course.myblog.repository.UserReportedRepository;
import it.course.myblog.repository.UserRepository;
import it.course.myblog.security.JwtUser;
import it.course.myblog.service.UserService;

@RestController
public class ReportCommentController {
	
	@Autowired
	CommentRepository commentRepository;
	
	@Autowired
	UserRepository userRepository;
	
	@Autowired
	ReportCommentRepository reportCommentRepository;
	
	@Autowired
	UserService userService;
	
	@Autowired
	UserReportedRepository userReportedRepository;
	
	@PostMapping("private/create-comment-report")
	@PreAuthorize("hasRole('READER')")
	public ResponseEntity<ApiResponseCustom> createCommentReport(@Valid @RequestBody ReportCommentRequest reportCommentRequest, HttpServletRequest request){
		
		Optional<Comment> c = commentRepository.findByIdAndVisibleTrue(reportCommentRequest.getCommentId());
		if(!c.isPresent())
			return new ResponseEntity<ApiResponseCustom>(new ApiResponseCustom (
					Instant.now(), 200, "OK", "Comment not found", request.getRequestURI()), HttpStatus.OK);
		
		
		JwtUser jwtUser = userService.getAuthenticatedUser();
		Optional<User> reporter = userRepository.findByUsername(jwtUser.getUsername());
		
		Optional<ReportComment> reportCommentToFind = reportCommentRepository.findById(new ReportCommentCompositeKey(reporter.get(), c.get()));
		if(reportCommentToFind.isPresent())
			return new ResponseEntity<ApiResponseCustom>(new ApiResponseCustom (
					Instant.now(), 200, "OK", "The comment has been reported by you on "+reportCommentToFind.get().getCreatedAt(), request.getRequestURI()), HttpStatus.OK);
		
		ReportComment r = new ReportComment();
		r.setReportCommentCompositeKey(new ReportCommentCompositeKey(reporter.get(), c.get()));
		r.setReportReason(reportCommentRequest.getReportReason());
		
		reportCommentRepository.save(r);
		
		return new ResponseEntity<ApiResponseCustom>(new ApiResponseCustom (
				Instant.now(), 200, "OK", "Report for comment successfully created", request.getRequestURI()), HttpStatus.OK);
		
		
	}
	
	@PutMapping("private/update-comment-report")
	@PreAuthorize("hasRole('ADMIN')")
	@Transactional
	public ResponseEntity<ApiResponseCustom> updateCommentReport(@Valid @RequestBody ReportCommentNoteRequest reportCommentNoteRequest, HttpServletRequest request){
		
		Optional<Comment> c = commentRepository.findByIdAndVisibleTrue(reportCommentNoteRequest.getCommentId());
		if(!c.isPresent())
			return new ResponseEntity<ApiResponseCustom>(new ApiResponseCustom (
					Instant.now(), 200, "OK", "Comment not found", request.getRequestURI()), HttpStatus.OK);
		
		Optional<User> u = userRepository.findById(reportCommentNoteRequest.getReporterId());
		if(!u.isPresent())
			return new ResponseEntity<ApiResponseCustom>(new ApiResponseCustom (
					Instant.now(), 200, "OK", "User not found", request.getRequestURI()), HttpStatus.OK);
		
		Optional<ReportComment> reportCommentToFind = reportCommentRepository.findById(new ReportCommentCompositeKey(u.get(), c.get()));
		if(!reportCommentToFind.isPresent())
			return new ResponseEntity<ApiResponseCustom>(new ApiResponseCustom (
					Instant.now(), 200, "OK", "Report comment not found", request.getRequestURI()), HttpStatus.OK);
		
		reportCommentToFind.get().setReportNote(reportCommentNoteRequest.getReportNote());
		
		reportCommentRepository.save(reportCommentToFind.get());
		
		if(reportCommentNoteRequest.isValid()) {
			
			// set comment not visibile
			c.get().setVisible(false);
			commentRepository.save(c.get());
			
			//ctrl if the author must be banned
			Optional<UserReported> ur = userReportedRepository.findByUserReportedId(new UserReportedId(c.get().getCommentAuthor()));
			if(ur.isPresent()) {
				if(ur.get().getReportNumber() < 2) {
					ur.get().setReportNumber(ur.get().getReportNumber() + 1);
					userReportedRepository.save(ur.get());
				} else {
					Optional<User> user = userRepository.findById(c.get().getCommentAuthor().getId());
					user.get().setEnabled(false);
					userRepository.save(user.get());
				}
				
			} else {
				userReportedRepository.save(new UserReported(new UserReportedId(c.get().getCommentAuthor()),1));
			}
		}
		
		return new ResponseEntity<ApiResponseCustom>(new ApiResponseCustom (
				Instant.now(), 200, "OK", "Report comment successfully update", request.getRequestURI()), HttpStatus.OK);
		
	}
	
	@GetMapping("private/get-reports-comment-not-checked")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<ApiResponseCustom> getReportsCommentNotChecked(HttpServletRequest request) {
		
		List<ReportComment> reports = reportCommentRepository.findByReportNoteIsNullOrderByCreatedAtAsc();
		if (reports.isEmpty())
			return new ResponseEntity<ApiResponseCustom>(new ApiResponseCustom(Instant.now(), 200, "OK",
					"No reports comment waiting for check", request.getRequestURI()), HttpStatus.OK);

		List<ReportCommentDetailResponse> rpsdet = reports.stream().map(ReportCommentDetailResponse::createFromReportComment).collect(Collectors.toList());
		return new ResponseEntity<ApiResponseCustom>(
				new ApiResponseCustom(Instant.now(), 200, "OK", rpsdet, request.getRequestURI()), HttpStatus.OK);

	}
	
	@GetMapping("private/get-reports-comment-by-reporter")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<ApiResponseCustom> getReportsCommentByReporter(@RequestParam String reporterUsername, HttpServletRequest request){
		
		Optional<User> u = userRepository.findByUsername(reporterUsername);
		if(!u.isPresent())
			return new ResponseEntity<ApiResponseCustom>(new ApiResponseCustom (
					Instant.now(), 200, "OK", "User not found", request.getRequestURI()), HttpStatus.OK);
		
		List<ReportComment> reports = reportCommentRepository.findByReportCommentCompositeKeyReporter(u.get());
		if (reports.isEmpty())
			return new ResponseEntity<ApiResponseCustom>(new ApiResponseCustom(Instant.now(), 200, "OK",
					"No reports comment found for reporter : "+reporterUsername, request.getRequestURI()), HttpStatus.OK);
		
		List<ReportCommentDetailResponse> rpsdet = reports.stream().map(ReportCommentDetailResponse::createFromReportComment).collect(Collectors.toList());
		return new ResponseEntity<ApiResponseCustom>(
				new ApiResponseCustom(Instant.now(), 200, "OK", rpsdet, request.getRequestURI()), HttpStatus.OK);

	}
	
	@GetMapping("private/count-reports-comment-by-comment")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<ApiResponseCustom> countReportsCommentByComment(@RequestParam Long id, HttpServletRequest request){
		
		Optional<Comment> c = commentRepository.findByIdAndVisibleTrue(id);
		if(!c.isPresent())
			return new ResponseEntity<ApiResponseCustom>(new ApiResponseCustom (
					Instant.now(), 200, "OK", "Comment not found", request.getRequestURI()), HttpStatus.OK);
		
		
		CountReportsCommentByCommentResponse cr = new CountReportsCommentByCommentResponse(
				c.get().getId(),
				c.get().getComment(),
				reportCommentRepository.countByReportCommentCompositeKeyReportedComment(c.get())
				);		
		
		return new ResponseEntity<ApiResponseCustom>(
				new ApiResponseCustom(Instant.now(), 200, "OK", cr, request.getRequestURI()), HttpStatus.OK);
		
	}
		
	

	@GetMapping("private/count-reports-group-by-comments")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<ApiResponseCustom> countReportsCommentGroupByComments(HttpServletRequest request){
		
		List<ReportComment> reports = reportCommentRepository.findAll();
		if (reports.isEmpty())
			return new ResponseEntity<ApiResponseCustom>(new ApiResponseCustom(Instant.now(), 200, "OK",
					"No reports comment found", request.getRequestURI()), HttpStatus.OK);
		
		Set<Comment> comments = reports.stream()
				.map(p -> p.getReportCommentCompositeKey().getReportedComment())
				.collect(Collectors.toSet());
		
		List<CountReportsCommentByCommentResponse> crs = comments.stream().map(c ->new CountReportsCommentByCommentResponse(
				c.getId(),
				c.getComment(),
				reportCommentRepository.countByReportCommentCompositeKeyReportedComment(c)
				)).collect(Collectors.toList());
		
		return new ResponseEntity<ApiResponseCustom>(
				new ApiResponseCustom(Instant.now(), 200, "OK", crs, request.getRequestURI()), HttpStatus.OK);
		
	}
	
	@GetMapping("private/count-reports-comment-group-by-reporter")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<ApiResponseCustom> countReportsCommentGroupByReporter(HttpServletRequest request){
		
		List<ReportComment> reports = reportCommentRepository.findAll();
		if (reports.isEmpty())
			return new ResponseEntity<ApiResponseCustom>(new ApiResponseCustom(Instant.now(), 200, "OK",
					"No reports comment found", request.getRequestURI()), HttpStatus.OK);
		
		Set<User> users = reports.stream()
				.map(c -> c.getReportCommentCompositeKey().getReporter())
				.collect(Collectors.toSet());
		
		List<CountReportsByReporterResponse> crs = users.stream().map(u ->new CountReportsByReporterResponse(
				u.getId(),
				u.getUsername(),
				reportCommentRepository.countByReportCommentCompositeKeyReporter(u)
				)).collect(Collectors.toList());
		
		return new ResponseEntity<ApiResponseCustom>(
				new ApiResponseCustom(Instant.now(), 200, "OK", crs, request.getRequestURI()), HttpStatus.OK);
		
	}
	
	
}
