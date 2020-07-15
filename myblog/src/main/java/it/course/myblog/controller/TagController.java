package it.course.myblog.controller;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


import it.course.myblog.entity.Tag;
import it.course.myblog.payload.request.wrapper.StringWrapper;
import it.course.myblog.payload.response.ApiResponseCustom;
import it.course.myblog.repository.TagRepository;

@RestController
//@Validated
public class TagController {
	
	@Autowired
	TagRepository tagRepository;
	
	
	@PostMapping("private/create-tags")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<ApiResponseCustom> createTags(@RequestBody StringWrapper strings, HttpServletRequest request){
		
		if(strings.getStrings().isEmpty())
			return new ResponseEntity<ApiResponseCustom>(new ApiResponseCustom (
					Instant.now(), 200, "OK", "No tags present in request", request.getRequestURI()), HttpStatus.OK);	
		
		Set<String> tagsToUppercase = strings.getStrings().stream().map(String::toUpperCase).collect(Collectors.toSet());
		
		List<Tag> existentTags = tagRepository.findByTagNameIn(tagsToUppercase);
		if(!existentTags.isEmpty())
			return new ResponseEntity<ApiResponseCustom>(new ApiResponseCustom (
					Instant.now(), 200, "OK", "Tags present: "+existentTags.toString(), request.getRequestURI()), HttpStatus.OK);	
		
		List<Tag> tagsToCreate =  tagsToUppercase.stream().map(Tag::new).collect(Collectors.toList());
		
		tagRepository.saveAll(tagsToCreate);		
		
		return new ResponseEntity<ApiResponseCustom>(new ApiResponseCustom (
				Instant.now(), 200, "OK", "Tags have been succesfully created", request.getRequestURI()), HttpStatus.OK);	
		
	}
	
	@GetMapping("private/get-all-tags")
	@PreAuthorize("hasRole('ADMIN') or hasRole('EDITOR')")
	public ResponseEntity<ApiResponseCustom> getAllTags(HttpServletRequest request){
		
		List<Tag> tags = tagRepository.findAllByOrderByTagNameAsc();
		
		if(tags.isEmpty())
			return new ResponseEntity<ApiResponseCustom>(new ApiResponseCustom (
					Instant.now(), 200, "OK", "No tags found", request.getRequestURI()), HttpStatus.OK);	
		
		return new ResponseEntity<ApiResponseCustom>(new ApiResponseCustom (
				Instant.now(), 200, "OK", tags, request.getRequestURI()), HttpStatus.OK);	
		
	}
	
	@GetMapping("private/get-all-published-tags")
	@PreAuthorize("hasRole('ADMIN') or hasRole('EDITOR')")
	public ResponseEntity<ApiResponseCustom> getAllPublishedTags(HttpServletRequest request){
		
		List<Tag> tags = tagRepository.findAllByVisibleTrueOrderByTagNameAsc();
		
		if(tags.isEmpty())
			return new ResponseEntity<ApiResponseCustom>(new ApiResponseCustom (
					Instant.now(), 200, "OK", "No tags found", request.getRequestURI()), HttpStatus.OK);	
		
		return new ResponseEntity<ApiResponseCustom>(new ApiResponseCustom (
				Instant.now(), 200, "OK", tags, request.getRequestURI()), HttpStatus.OK);	
		
	}
	
	@GetMapping("private/get-all-unpublished-tags")
	@PreAuthorize("hasRole('ADMIN') or hasRole('EDITOR')")
	public ResponseEntity<ApiResponseCustom> getAllUnpublishedTags(HttpServletRequest request){
		
		List<Tag> tags = tagRepository.findAllByVisibleFalseOrderByTagNameAsc();
		
		if(tags.isEmpty())
			return new ResponseEntity<ApiResponseCustom>(new ApiResponseCustom (
					Instant.now(), 200, "OK", "No tags found", request.getRequestURI()), HttpStatus.OK);	
		
		return new ResponseEntity<ApiResponseCustom>(new ApiResponseCustom (
				Instant.now(), 200, "OK", tags, request.getRequestURI()), HttpStatus.OK);	
		
	}
	
	@PutMapping("private/publish-tags")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<ApiResponseCustom> publishTags(@RequestBody StringWrapper strings, HttpServletRequest request){
		
		if(strings.getStrings().isEmpty())
			return new ResponseEntity<ApiResponseCustom>(new ApiResponseCustom (
					Instant.now(), 200, "OK", "No tags present in request", request.getRequestURI()), HttpStatus.OK);	
		
		Set<String> tagsToUppercase = strings.getStrings().stream().map(String::toUpperCase).collect(Collectors.toSet());
		
		List<Tag> tagsToPublish = tagRepository.findByTagNameIn(tagsToUppercase);
		if(tagsToPublish.isEmpty())
			return new ResponseEntity<ApiResponseCustom>(new ApiResponseCustom (
					Instant.now(), 200, "OK", "No tags found", request.getRequestURI()), HttpStatus.OK);	
		
		tagsToPublish.forEach(t -> t.setVisible(true));
		
		tagRepository.saveAll(tagsToPublish);
		
		return new ResponseEntity<ApiResponseCustom>(new ApiResponseCustom (
				Instant.now(), 200, "OK", "Tags have been published", request.getRequestURI()), HttpStatus.OK);	
		
	}
	
	@PutMapping("private/unpublish-tags")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<ApiResponseCustom> unPublishTags(@RequestBody StringWrapper strings, HttpServletRequest request){
		
		if(strings.getStrings().isEmpty())
			return new ResponseEntity<ApiResponseCustom>(new ApiResponseCustom (
					Instant.now(), 200, "OK", "No tags present in request", request.getRequestURI()), HttpStatus.OK);	
		
		Set<String> tagsToUppercase = strings.getStrings().stream().map(String::toUpperCase).collect(Collectors.toSet());
		
		List<Tag> tagsToUnpublish = tagRepository.findByTagNameIn(tagsToUppercase);
		if(tagsToUnpublish.isEmpty())
			return new ResponseEntity<ApiResponseCustom>(new ApiResponseCustom (
					Instant.now(), 200, "OK", "No tags found", request.getRequestURI()), HttpStatus.OK);	
		
		tagsToUnpublish.forEach(t -> t.setVisible(false));
		
		tagRepository.saveAll(tagsToUnpublish);
		
		return new ResponseEntity<ApiResponseCustom>(new ApiResponseCustom (
				Instant.now(), 200, "OK", "Tags have been unpublished", request.getRequestURI()), HttpStatus.OK);	
		
	}
	
	@PutMapping("private/update-tag")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<ApiResponseCustom> updateTag(@RequestParam @NotBlank String oldTagName, 
			@RequestParam @NotBlank(message="new tag is mandatory") String newTagName, HttpServletRequest request){
		
		if(oldTagName == null || oldTagName.trim().length() < 1)
			return new ResponseEntity<ApiResponseCustom>(new ApiResponseCustom (
					Instant.now(), 400, "Bad Request", "oldTagName is mandatory", request.getRequestURI()), HttpStatus.BAD_REQUEST);
		
		if(newTagName == null || newTagName.trim().length() < 1)
			return new ResponseEntity<ApiResponseCustom>(new ApiResponseCustom (
					Instant.now(), 400, "Bad Request", "newTagName is mandatory", request.getRequestURI()), HttpStatus.BAD_REQUEST);
		
		Optional<Tag> oldTag = tagRepository.findByTagName(oldTagName.toUpperCase());
		if(!oldTag.isPresent())
			return new ResponseEntity<ApiResponseCustom>(new ApiResponseCustom (
					Instant.now(), 200, "OK", "No tag found", request.getRequestURI()), HttpStatus.OK);
		
		Optional<Tag> newTag = tagRepository.findByTagName(newTagName.toUpperCase());
		if(newTag.isPresent())
			return new ResponseEntity<ApiResponseCustom>(new ApiResponseCustom (
					Instant.now(), 200, "OK", "The tag is already present", request.getRequestURI()), HttpStatus.OK);
		
		oldTag.get().setTagName(newTagName.toUpperCase());
		
		tagRepository.save(oldTag.get());		
		
		return new ResponseEntity<ApiResponseCustom>(new ApiResponseCustom (
				Instant.now(), 200, "OK", "The tag has been updated", request.getRequestURI()), HttpStatus.OK);
	}
}
