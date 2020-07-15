package it.course.myblog.controller;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import it.course.myblog.entity.Comment;
import it.course.myblog.entity.Post;
import it.course.myblog.entity.User;
import it.course.myblog.payload.request.CommentRequest;
import it.course.myblog.payload.response.ApiResponseCustom;
import it.course.myblog.repository.CommentRepository;
import it.course.myblog.repository.PostRepository;
import it.course.myblog.repository.UserRepository;
import it.course.myblog.security.JwtUser;
import it.course.myblog.service.UserService;

@RestController
public class CommentController {
	
	@Autowired
	UserService userService;
	
	@Autowired
	UserRepository userRepository;
	
	@Autowired
	CommentRepository commentRepository;
	
	@Autowired
	PostRepository postRepository;
	
	
	@PostMapping("private/create-comment")
	@PreAuthorize("hasRole('READER') or hasRole('EDITOR')")
	@Transactional
	public ResponseEntity<ApiResponseCustom> createComment(@RequestBody CommentRequest commentRequest, HttpServletRequest request){
		
		JwtUser jwtUser = userService.getAuthenticatedUser();
		Optional<User> commentAuthor = userRepository.findByUsername(jwtUser.getUsername());
		
		Comment referenceComment = null;
		if(commentRequest.getReferenceComment() != null) 
			referenceComment = commentRepository.findById(commentRequest.getReferenceComment()).get();
		
		Comment c = new Comment();
		c.setComment(commentRequest.getComment());
		c.setCommentAuthor(commentAuthor.get());
		c.setReferenceComment(referenceComment);
		commentRepository.save(c);
		
		Optional<Post> p = postRepository.findById(commentRequest.getPostId());
		List<Comment> cs = p.get().getComments();
		cs.add(c);
		p.get().setComments(cs);
		postRepository.save(p.get());
		
		return new ResponseEntity<ApiResponseCustom>(
			new ApiResponseCustom(Instant.now(), 200, null, "New comment to post "+p.get().getTitle()+" successfully created", request.getRequestURI()),
			HttpStatus.OK);
	}

	
	@PutMapping("private/hide-show-comment/{id}")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<ApiResponseCustom> hideShowComment(@PathVariable Long id, HttpServletRequest request){
		
		Optional<Comment> c = commentRepository.findById(id);
		if (!c.isPresent())
			return new ResponseEntity<ApiResponseCustom> (new ApiResponseCustom (
					Instant.now(),200, null , "Comment not found" , request.getRequestURI()), HttpStatus.OK);
		
		c.get().setVisible(!c.get().getVisible());
		commentRepository.save(c.get());
		
		return new ResponseEntity<ApiResponseCustom>(
			new ApiResponseCustom(Instant.now(), 200, null, "Change status: ok", request.getRequestURI()),
			HttpStatus.OK);
	}
}
