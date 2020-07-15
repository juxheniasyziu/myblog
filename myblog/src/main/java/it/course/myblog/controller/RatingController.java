package it.course.myblog.controller;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import it.course.myblog.entity.Post;
import it.course.myblog.entity.Rating;
import it.course.myblog.entity.RatingUserPostCompositeKey;
import it.course.myblog.entity.User;
import it.course.myblog.payload.response.ApiResponseCustom;
import it.course.myblog.repository.PostRepository;
import it.course.myblog.repository.RatingRepository;
import it.course.myblog.repository.UserRepository;
import it.course.myblog.security.JwtUser;
import it.course.myblog.service.UserService;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
public class RatingController {
	
	@Autowired
	RatingRepository ratingRepository;
	
	@Autowired
	PostRepository postRepository;
	
	@Autowired
	UserRepository userRepository;
	
	@Autowired
	UserService userService;
	
	
	@PostMapping("private/rate-post/{postId}")
	@Transactional
	public ResponseEntity<ApiResponseCustom> ratePost(@PathVariable long postId, @RequestParam double rate, HttpServletRequest request) {
		
		if(rate < 1.00 || rate > 5.00)
			return new ResponseEntity<ApiResponseCustom>(new ApiResponseCustom (
					Instant.now(), 200, "OK", "The rate must be between 1 and 5", request.getRequestURI()), HttpStatus.OK);
		
		Optional<Post> p = postRepository.findByIdAndVisibleTrue(postId);
		if(!p.isPresent())
			return new ResponseEntity<ApiResponseCustom>(new ApiResponseCustom (
					Instant.now(), 200, "OK", "Post not found", request.getRequestURI()), HttpStatus.OK);
		
		JwtUser jwtUser = userService.getAuthenticatedUser();
		Optional<User> u = userRepository.findByUsername(jwtUser.getUsername());
		if(!u.isPresent())
			return new ResponseEntity<ApiResponseCustom>(new ApiResponseCustom (
					Instant.now(), 200, "OK", "User not found", request.getRequestURI()), HttpStatus.OK);
		
		if(u.get().getId().equals(p.get().getAuthor().getId()))
			return new ResponseEntity<ApiResponseCustom>(new ApiResponseCustom (
					Instant.now(), 200, "OK", p.get().getAuthor().getUsername()+" you cannot rate this post because you write it", request.getRequestURI()), HttpStatus.OK);
		
		Optional<Rating> r = ratingRepository.findById(new RatingUserPostCompositeKey(p.get(), u.get()));
		String x = "added";
		if(!r.isPresent()) {
			ratingRepository.save(new Rating(new RatingUserPostCompositeKey(p.get(), u.get()), rate));
		} else {
			r.get().setRating(rate);
			ratingRepository.save(r.get());
			x = "updated";
		}
		
		Instant start1 = Instant.now();
		
		// calc AVG and update it 
		
		List<Rating> rs = ratingRepository.findByRatingUserPostCompositeKeyPost(p.get());
		double avg = rs.stream().mapToDouble(Rating::getRating).average().getAsDouble();
		p.get().setAvgRating(avg);
		Instant end1 = Instant.now();
		
		Instant start2 = Instant.now();
		double avgRating = ratingRepository.getAvgByPost(p.get());
		p.get().setAvgRating(avgRating);
		
		Instant end2 = Instant.now();
		
		log.info("--- JAVA --- "+Duration.between(start1, end1).toMillis());
		log.info("--- MYSQL--- "+Duration.between(start2, end2).toMillis());
		//long timeElapsed = Duration.between(start1, end1).toMillis();
		
		postRepository.save(p.get());
		
		return new ResponseEntity<ApiResponseCustom>(new ApiResponseCustom (
				Instant.now(), 200, "OK", "The rate has been successfully "+x, request.getRequestURI()), HttpStatus.OK);
		
	}
	

}
