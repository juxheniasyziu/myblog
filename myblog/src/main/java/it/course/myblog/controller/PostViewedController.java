package it.course.myblog.controller;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import it.course.myblog.entity.Post;
import it.course.myblog.entity.PostViewed;
import it.course.myblog.payload.request.PostViewedRequest;
import it.course.myblog.payload.response.ApiResponseCustom;
import it.course.myblog.payload.response.PostViewedByPostResponse;
import it.course.myblog.payload.response.PostViewedUniquePostResponse;
import it.course.myblog.payload.response.PostViewedUniqueResponse;
import it.course.myblog.repository.PostRepository;
import it.course.myblog.repository.PostViewedRepository;

@RestController
public class PostViewedController {
	
	@Autowired
	PostViewedRepository postViewedRepository;
	
	@Autowired
	PostRepository postRepository;
	
	@GetMapping("private/get-count-by-posts-viewed")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<ApiResponseCustom> getCountByPostViewed(@RequestBody PostViewedRequest postViewedRequest, HttpServletRequest request){
		
		if(postViewedRequest.getFrom().after(postViewedRequest.getTo()))
			return new ResponseEntity<ApiResponseCustom>(
					new ApiResponseCustom(Instant.now(), 400, "Start date cannot be after End date", null, request.getRequestURI()), HttpStatus.BAD_REQUEST);
			
		
		List<PostViewed> pvs = postViewedRepository.postViewedGroupedByPost(postViewedRequest.getFrom(), postViewedRequest.getTo());
		
		if(pvs.isEmpty())
			return new ResponseEntity<ApiResponseCustom>(
				new ApiResponseCustom(Instant.now(), 200, "OK", "No Posts viewed between "+postViewedRequest.getFrom()+" and "+postViewedRequest.getTo(), request.getRequestURI()), HttpStatus.OK);
		
		List<PostViewedByPostResponse> pvrs = pvs.stream().map(PostViewedByPostResponse::createFromEntity).collect(Collectors.toList());
		
		return new ResponseEntity<ApiResponseCustom>(
			new ApiResponseCustom(Instant.now(), 200, "OK", pvrs, request.getRequestURI()), HttpStatus.OK);
		
		
	}
	
	@GetMapping("private/get-count-by-posts-viewed-bi")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<ApiResponseCustom> getCountByPostViewedBi(@RequestBody PostViewedRequest postViewedRequest, HttpServletRequest request){
		
		if(postViewedRequest.getFrom().after(postViewedRequest.getTo()))
			return new ResponseEntity<ApiResponseCustom>(
					new ApiResponseCustom(Instant.now(), 400, "Start date cannot be after End date", null, request.getRequestURI()), HttpStatus.BAD_REQUEST);
		
		List<PostViewed> pvs = postViewedRepository.findByCreatedAtBetween(postViewedRequest.getFrom(), postViewedRequest.getTo());
		
		if(pvs.isEmpty())
			return new ResponseEntity<ApiResponseCustom>(
				new ApiResponseCustom(Instant.now(), 200, "OK", "No Posts viewed between "+postViewedRequest.getFrom()+" and "+postViewedRequest.getTo(), request.getRequestURI()), HttpStatus.OK);
		
		Map<PostViewedByPostResponse, Long> pvrsMap = pvs.stream()
				.collect(Collectors.groupingBy(PostViewedByPostResponse::createFromEntity, Collectors.counting()));
		
		pvrsMap.entrySet().stream()
			.forEach(x -> x.getKey().setTotalView(x.getValue()));
		
		List<PostViewedByPostResponse> setKey = pvrsMap.keySet()
				.stream()
				//.sorted((p1, p2)->p2.getTotalView().compareTo(p1.getTotalView()))
				.sorted(Comparator.comparingLong(x -> x.getTotalView()*-1))
				.collect(Collectors.toList());
				
		return new ResponseEntity<ApiResponseCustom>(
			new ApiResponseCustom(Instant.now(), 200, "OK", setKey, request.getRequestURI()), HttpStatus.OK);
		
		
	}
	
	@GetMapping("private/get-count-unique-visit-bi")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<ApiResponseCustom> getCountUniqueVisitBi(@RequestBody PostViewedRequest postViewedRequest, HttpServletRequest request){
		
		if(postViewedRequest.getFrom().after(postViewedRequest.getTo()))
			return new ResponseEntity<ApiResponseCustom>(
					new ApiResponseCustom(Instant.now(), 400, "Start date cannot be after End date", null, request.getRequestURI()), HttpStatus.BAD_REQUEST);
		
		
		List<PostViewed> pvs = postViewedRepository.findByCreatedAtBetween(postViewedRequest.getFrom(), postViewedRequest.getTo());
		
		if(pvs.isEmpty())
			return new ResponseEntity<ApiResponseCustom>(
				new ApiResponseCustom(Instant.now(), 200, "OK", "No Posts viewed between "+postViewedRequest.getFrom()+" and "+postViewedRequest.getTo(), request.getRequestURI()), HttpStatus.OK);
		
		// grouping postId, viewer, ip
		
		Map<PostViewedUniqueResponse, Long> pvrsMap = pvs.stream()
				.collect(Collectors.groupingBy(PostViewedUniqueResponse::createFromEntity, Collectors.counting()));
		
		pvrsMap.entrySet().stream()
			.forEach(x -> x.getKey().setTotalView(x.getValue()));
		
		List<PostViewedUniqueResponse> setKey = pvrsMap.keySet()
				.stream()
				//.sorted((p1, p2)->p2.getTotalView().compareTo(p1.getTotalView()))
				.sorted(Comparator.comparingLong(x -> x.getTotalView()*-1))
				.collect(Collectors.toList());
		
		
		// grouping postId

		
		Map<PostViewedUniquePostResponse, Long> pvuMap = setKey.stream()
				.collect(Collectors.groupingBy(PostViewedUniquePostResponse::createFromPostViewedUniqueResponse, Collectors.counting()));
		
		pvuMap.entrySet().stream()
			.forEach(x -> x.getKey().setTotalView(x.getValue()));
		
		List<PostViewedUniquePostResponse> setKeyPu = pvuMap.keySet()
				.stream()
				//.sorted((p1, p2)->p2.getTotalView().compareTo(p1.getTotalView()))
				.sorted(Comparator.comparingLong(x -> x.getTotalView()*-1))
				.collect(Collectors.toList());
		
		
		return new ResponseEntity<ApiResponseCustom>(
				new ApiResponseCustom(Instant.now(), 200, "OK", setKeyPu, request.getRequestURI()), HttpStatus.OK);
			
		
	}
	
	@GetMapping("private/get-count-unique-visit-by-post-bi/{id}")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<ApiResponseCustom> getCountUniqueVisitByPostBi(@PathVariable Long id,
			@RequestBody PostViewedRequest postViewedRequest, HttpServletRequest request){
		
		if(postViewedRequest.getFrom().after(postViewedRequest.getTo()))
			return new ResponseEntity<ApiResponseCustom>(
					new ApiResponseCustom(Instant.now(), 400, "Start date cannot be after End date", null, request.getRequestURI()), HttpStatus.BAD_REQUEST);
		
		Optional<Post> p = postRepository.findById(id);
		if(!p.isPresent())
			return new ResponseEntity<ApiResponseCustom>(new ApiResponseCustom (
					Instant.now(), 200, "OK", "Post not found", request.getRequestURI()), HttpStatus.OK);
		
		PostViewed pv = postViewedRepository.postViewedByPost(postViewedRequest.getFrom(), postViewedRequest.getTo(), p.get());
		if(pv == null)
			return new ResponseEntity<ApiResponseCustom>(new ApiResponseCustom (
					Instant.now(), 200, "OK", "The post has never been visited", request.getRequestURI()), HttpStatus.OK);
		
		PostViewedByPostResponse pvr = PostViewedByPostResponse.createFromEntity(pv);
		
		return new ResponseEntity<ApiResponseCustom>(
				new ApiResponseCustom(Instant.now(), 200, "OK", pvr, request.getRequestURI()), HttpStatus.OK);
	}

}
