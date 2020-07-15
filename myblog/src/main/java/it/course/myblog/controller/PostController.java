package it.course.myblog.controller;

import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import it.course.myblog.entity.DBFile;
import it.course.myblog.entity.Post;
import it.course.myblog.entity.PostViewed;
import it.course.myblog.entity.Rating;
import it.course.myblog.entity.Tag;
import it.course.myblog.entity.User;
import it.course.myblog.payload.request.wrapper.SetTagWrapper;
import it.course.myblog.payload.request.wrapper.StringWrapper;
import it.course.myblog.payload.response.ApiResponseCustom;
import it.course.myblog.payload.response.CommentResponse;
import it.course.myblog.payload.response.PostByAvgAndTotal;
import it.course.myblog.payload.response.PostDetail;
import it.course.myblog.payload.response.PostToVerifyOrToPublish;
import it.course.myblog.payload.response.PostsHomeResponse;
import it.course.myblog.repository.DBFileRepository;
import it.course.myblog.repository.PostRepository;
import it.course.myblog.repository.PostViewedRepository;
import it.course.myblog.repository.RatingRepository;
import it.course.myblog.repository.TagRepository;
import it.course.myblog.repository.UserRepository;
import it.course.myblog.security.JwtTokenUtil;
import it.course.myblog.security.JwtUser;
import it.course.myblog.service.DBFileService;
import it.course.myblog.service.PDFService;
import it.course.myblog.service.PostService;
import it.course.myblog.service.UserService;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
public class PostController {

	@Autowired
	PostRepository postRepository;

	@Autowired
	TagRepository tagRepository;

	@Autowired
	UserRepository userRepository;

	@Autowired
	UserService userService;

	@Autowired
	PostService postService;

	@Autowired
	DBFileService dbFileService;

	@Autowired
	DBFileRepository dbFileRepository;

	@Autowired
	JwtTokenUtil jwtTokenUtil;

	@Autowired
	PostViewedRepository postViewedRepository;

	@Autowired
	RatingRepository ratingRepository;

	@Autowired
	PDFService pdfService;

	@PostMapping("private/create-post-with-image")
	@PreAuthorize("hasRole('EDITOR')")
	@Transactional
	public ResponseEntity<ApiResponseCustom> createPostWithImage(@RequestPart("dbFile") MultipartFile dbFile,
			@RequestParam String title, @RequestParam String content, @RequestParam String overview,
			// @RequestParam StringWrapper tags,
			@RequestParam Set<String> tags, HttpServletRequest request) throws Exception {

		DBFile file = null;
		if (!dbFile.isEmpty()) {

			// Returns DBFile from request
			file = dbFileService.fromMultiToDBFile(dbFile);

			// Transforms Multipart file in a buffered image in order to control height and
			// width
			BufferedImage image = dbFileService.getBufferedImage(dbFile);

			if (dbFileService.ctrlImageSize(image) != null) {
				return new ResponseEntity<ApiResponseCustom>(new ApiResponseCustom(Instant.now(), 401, null,
						dbFileService.ctrlImageSize(image), request.getRequestURI()), HttpStatus.FORBIDDEN);
			}

			// save file into database
			dbFileRepository.save(file);
		}
		log.info(" \n\n-----> NO IMAGE WAS SELECTED FOR THIS POST\n");

		// find the logged editor as post author
		JwtUser jwtUser = userService.getAuthenticatedUser();
		Optional<User> author = userRepository.findByUsername(jwtUser.getUsername());

		// Convert all tag passed in upper case
		Set<String> tagsToUpperCase = tags.stream().map(String::toUpperCase).collect(Collectors.toSet());

		// find all tag already exist in database
		List<Tag> tagsToAdd = tagRepository.findByTagNameIn(tagsToUpperCase);
		Set<Tag> ts = tagsToAdd.stream().collect(Collectors.toSet());

		// Post creation
		Post p = new Post();
		p.setTitle(title);
		p.setContent(content);
		p.setOverview(overview);
		p.setDbFile(file);
		p.setAuthor(author.get());
		p.setTags(ts);

		// save post
		postRepository.save(p);

		return new ResponseEntity<ApiResponseCustom>(new ApiResponseCustom(Instant.now(), 200, null,
				"New post successfully created", request.getRequestURI()), HttpStatus.OK);
	}

	/*
	 * @PostMapping("private/create-post")
	 * 
	 * @PreAuthorize("hasRole('EDITOR')") public ResponseEntity<ApiResponseCustom>
	 * createPost(@Valid @RequestBody PostRequest postRequest, HttpServletRequest
	 * request){
	 * 
	 * Optional<Post> post = postRepository.findByTitle(postRequest.getTitle());
	 * 
	 * if(post.isPresent()) return new ResponseEntity<ApiResponseCustom>(new
	 * ApiResponseCustom ( Instant.now(), 200, "OK",
	 * "Post with title "+postRequest.getTitle()+" already exists",
	 * request.getRequestURI()), HttpStatus.OK);
	 * 
	 * Post p = Post.createFromRequest(postRequest);
	 * 
	 * JwtUser jwtUser = userService.getAuthenticatedUser(); Optional<User> author =
	 * userRepository.findByUsername(jwtUser.getUsername());
	 * 
	 * p.setAuthor(author.get()); postRepository.save(p);
	 * 
	 * return new ResponseEntity<ApiResponseCustom>(new ApiResponseCustom (
	 * Instant.now(), 200, "OK", "Post succesfully created",
	 * request.getRequestURI()), HttpStatus.OK);
	 * 
	 * }
	 */
	@PutMapping("private/update-post/{id}")
	@PreAuthorize("hasRole('EDITOR')")
	public ResponseEntity<ApiResponseCustom> updatePost(@PathVariable Long id,
			@RequestPart("dbFile") MultipartFile dbFile, @RequestParam String title, @RequestParam String content,
			@RequestParam String overview, @RequestParam Set<String> tags, HttpServletRequest request) {

		Optional<Post> p = postRepository.findById(id);

		if (!p.isPresent())
			return new ResponseEntity<ApiResponseCustom>(
					new ApiResponseCustom(Instant.now(), 200, "OK", "Post not found", request.getRequestURI()),
					HttpStatus.OK);

		JwtUser jwtUser = userService.getAuthenticatedUser();
		Optional<User> author = userRepository.findByUsername(jwtUser.getUsername());
		if (!p.get().getAuthor().equals(author.get()))
			return new ResponseEntity<ApiResponseCustom>(new ApiResponseCustom(Instant.now(), 200, "OK",
					"You are not the owner of this post", request.getRequestURI()), HttpStatus.OK);

		Optional<Post> postToVerify = postRepository.findByTitle(title);
		if (postToVerify.isPresent())
			if (postToVerify.get().getId() != id)
				return new ResponseEntity<ApiResponseCustom>(new ApiResponseCustom(Instant.now(), 200, "OK",
						"Post with title " + title + " already exists", request.getRequestURI()), HttpStatus.OK);

		if (dbFile.isEmpty()) {
			p.get().setDbFile(null);
		} else {
			// Returns DBFile from request
			DBFile file = dbFileService.fromMultiToDBFile(dbFile);

			// Transforms Multipart file in a buffered image in order to control height and
			// width

			BufferedImage image = dbFileService.getBufferedImage(dbFile);

			if (dbFileService.ctrlImageSize(image) != null) {
				return new ResponseEntity<ApiResponseCustom>(new ApiResponseCustom(Instant.now(), 401, null,
						dbFileService.ctrlImageSize(image), request.getRequestURI()), HttpStatus.FORBIDDEN);
			}

			if (p.get().getDbFile() != null) {
				Optional<DBFile> oldDBFile = dbFileRepository.findById(p.get().getDbFile().getId());
				if (oldDBFile.isPresent()) {
					oldDBFile.get().setFileName(file.getFileName());
					oldDBFile.get().setFileType(file.getFileType());
					oldDBFile.get().setData(file.getData());
					dbFileRepository.save(oldDBFile.get());
					p.get().setDbFile(oldDBFile.get());
				}
			} else {
				dbFileRepository.save(file);
				p.get().setDbFile(file);
			}
		}

		// Convert all tag passed in upper case
		Set<String> tagsToUpperCase = tags.stream().map(String::toUpperCase).collect(Collectors.toSet());

		// find all tag already exist in database
		List<Tag> tagsToAdd = tagRepository.findByTagNameIn(tagsToUpperCase);
		Set<Tag> ts = tagsToAdd.stream().collect(Collectors.toSet());

		p.get().setTitle(title);
		p.get().setOverview(overview);
		p.get().setContent(content);
		p.get().setApproved(false);
		p.get().setVisible(false);
		p.get().setTags(ts);

		postRepository.save(p.get());

		return new ResponseEntity<ApiResponseCustom>(
				new ApiResponseCustom(Instant.now(), 200, "OK", "Post succesfully updated", request.getRequestURI()),
				HttpStatus.OK);

	}

	@GetMapping("public/get-last-posts")
	public ResponseEntity<ApiResponseCustom> getLastPosts(HttpServletRequest request) {

		List<Post> ps = postRepository.findTop3ByVisibleTrueOrderByUpdatedAtDesc();

		if (ps.isEmpty())
			return new ResponseEntity<ApiResponseCustom>(new ApiResponseCustom(Instant.now(), 200, "OK",
					"No published posts found", request.getRequestURI()), HttpStatus.OK);

		List<PostsHomeResponse> phrs = ps.stream().map(PostsHomeResponse::createFromEntity)
				.collect(Collectors.toList());

		return new ResponseEntity<ApiResponseCustom>(
				new ApiResponseCustom(Instant.now(), 200, "OK", phrs, request.getRequestURI()), HttpStatus.OK);

	}

	@GetMapping("private/get-not-verified-or-published-posts/{code}")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<ApiResponseCustom> getNotVerifiedOrPublishedPosts(@PathVariable String code,
			HttpServletRequest request) {

		List<Post> ps = new ArrayList<Post>();

		// A = NOT APPROVED
		// V = NOT VERIFIED
		// AOV = NOT APPROVED OR VERIFIED
		// AAV = NOT APPROVED AND VERIFIED

		switch (code) {

		case "A":
			ps = postRepository.findAllByApprovedFalse();
			break;

		case "V":
			ps = postRepository.findAllByVisibleFalse();
			break;

		case "AOV":
			ps = postRepository.findAllByVisibleFalseOrApprovedFalse();
			break;

		case "AAV":
			ps = postRepository.findAllByVisibleFalseAndApprovedFalse();
			break;

		default:
			return new ResponseEntity<ApiResponseCustom>(
					new ApiResponseCustom(Instant.now(), 400, "Error PathVariable Specification Request!",
							"Insert one of this values (A - V - AOV - AAV)", request.getRequestURI()),
					HttpStatus.BAD_REQUEST);

		}

		if (ps.isEmpty())
			return new ResponseEntity<ApiResponseCustom>(new ApiResponseCustom(Instant.now(), 200,
					"No published and/or verified posts found!", null, request.getRequestURI()), HttpStatus.OK);

		List<PostToVerifyOrToPublish> phrs = ps.stream().map(PostToVerifyOrToPublish::createFromEntity)
				.collect(Collectors.toList());

		return new ResponseEntity<ApiResponseCustom>(
				new ApiResponseCustom(Instant.now(), 200, "OK", phrs, request.getRequestURI()), HttpStatus.OK);

	}

	/*
	 * @PutMapping("private/approve-post/{id}")
	 * 
	 * @PreAuthorize("hasRole('ADMIN')") public ResponseEntity<ApiResponseCustom>
	 * approvePost(@PathVariable Long id, HttpServletRequest request) {
	 * 
	 * Optional<Post> p = postRepository.findById(id);
	 * 
	 * p.get().setApproved(true); postRepository.save(p.get());
	 * 
	 * return new ResponseEntity<ApiResponseCustom>( new
	 * ApiResponseCustom(Instant.now(), 200, "OK", "Post "+id+" has been approved",
	 * request.getRequestURI()), HttpStatus.OK);
	 * 
	 * }
	 */

	@PutMapping("private/approve-massive-post")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<ApiResponseCustom> massiveApprovePosts(@Valid @RequestParam Set<Long> ids,
			HttpServletRequest request) {

		List<Post> ps = postRepository.findByIdIn(ids);
		ps.stream().forEach(p -> p.setApproved(true));

		// alternativa al foreach
		// ps.stream().peek(p -> p.setApproved(true)).collect(Collectors.toList());

		postRepository.saveAll(ps);

		return new ResponseEntity<ApiResponseCustom>(new ApiResponseCustom(Instant.now(), 200, "OK",
				"The Posts have been approved", request.getRequestURI()), HttpStatus.OK);

	}

	@PutMapping("private/disapprove-massive-post")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<ApiResponseCustom> massiveDisapprovePosts(@Valid @RequestParam Set<Long> ids,
			HttpServletRequest request) {

		List<Post> ps = postRepository.findByIdIn(ids);
		ps.stream().forEach(p -> p.setApproved(false));

		// alternativa al foreach
		// ps.stream().peek(p -> p.setApproved(true)).collect(Collectors.toList());

		postRepository.saveAll(ps);

		return new ResponseEntity<ApiResponseCustom>(new ApiResponseCustom(Instant.now(), 200, "OK",
				"The Posts have been approved", request.getRequestURI()), HttpStatus.OK);

	}

	@PutMapping("private/publish-massive-post")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<ApiResponseCustom> massivePublishPosts(@RequestParam Set<Long> ids,
			HttpServletRequest request) {

		List<Post> ps = postRepository.findByIdIn(ids);
		ps.stream().filter(p -> p.getApproved() == true).forEach(p -> p.setVisible(true));

		// alternativa al foreach
		// ps.stream().peek(p -> p.setApproved(true)).collect(Collectors.toList());
		postRepository.saveAll(ps);

		return new ResponseEntity<ApiResponseCustom>(
				new ApiResponseCustom(Instant.now(), 200, "OK", "The posts are now visible!", request.getRequestURI()),
				HttpStatus.OK);

	}

	@PutMapping("private/unpublish-massive-post")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<ApiResponseCustom> massiveUnpPublishPosts(@RequestParam Set<Long> ids,
			HttpServletRequest request) {

		List<Post> ps = postRepository.findByIdIn(ids);
		ps.stream().forEach(p -> p.setVisible(false));

		// alternativa al foreach
		// ps.stream().peek(p -> p.setApproved(true)).collect(Collectors.toList());
		postRepository.saveAll(ps);

		return new ResponseEntity<ApiResponseCustom>(
				new ApiResponseCustom(Instant.now(), 200, "OK", "The posts are now visible!", request.getRequestURI()),
				HttpStatus.OK);

	}

	@GetMapping("private/get-posts-by-author/{author}")
	public ResponseEntity<ApiResponseCustom> getPostsByAuthor(@PathVariable String author, HttpServletRequest request) {

		Optional<User> u = userRepository.findByUsername(author);

		List<Post> ps = postRepository.findByAuthorAndVisibleTrue(u.get());

		List<PostToVerifyOrToPublish> psa = ps.stream().map(PostToVerifyOrToPublish::createFromEntity)
				.collect(Collectors.toList());

		return new ResponseEntity<ApiResponseCustom>(
				new ApiResponseCustom(Instant.now(), 200, "OK", psa, request.getRequestURI()), HttpStatus.OK);

	}

	@GetMapping("public/get-post-detail/{id}")
	public ResponseEntity<ApiResponseCustom> getPostDetail(@PathVariable Long id, @RequestParam String ip,
			HttpServletRequest request) {

		Optional<Post> p = postRepository.findByIdAndVisibleTrue(id);
		if (!p.isPresent())
			return new ResponseEntity<ApiResponseCustom>(
					new ApiResponseCustom(Instant.now(), 200, "OK", "Post not found", request.getRequestURI()),
					HttpStatus.OK);

		List<CommentResponse> comments = p.get().getComments().stream().map(CommentResponse::createFromEntity)
				.collect(Collectors.toList());
		PostDetail pd = PostDetail.createFromEntity(p.get(), comments);

		// post viewed
		String viewer = jwtTokenUtil.getUsernameFromToken(request.getHeader("X-Auth"));
		viewer = viewer == null ? "anonymous" : viewer;

		if (!postViewedRepository.existsByPostAndViewerAndIpAndCreatedAtBetween(p.get(), viewer, ip,
				postService.limitOfDay(Date.from(Instant.now()), "S"),
				postService.limitOfDay(Date.from(Instant.now()), "E")))
			postViewedRepository.save(new PostViewed(p.get(), ip, viewer, 0L));

		return new ResponseEntity<ApiResponseCustom>(
				new ApiResponseCustom(Instant.now(), 200, "OK", pd, request.getRequestURI()), HttpStatus.OK);

	}

	@PutMapping("private/update-post-with-tags/{id}")
	@PreAuthorize("hasRole('EDITOR')")
	public ResponseEntity<ApiResponseCustom> updatePostWithTags(@RequestBody SetTagWrapper tags, @PathVariable Long id,
			HttpServletRequest request) {

		if (tags.getTags().isEmpty())
			return new ResponseEntity<ApiResponseCustom>(
					new ApiResponseCustom(Instant.now(), 200, "OK", "No tags selected", request.getRequestURI()),
					HttpStatus.OK);

		Optional<Post> p = postRepository.findById(id);
		if (!p.isPresent())
			return new ResponseEntity<ApiResponseCustom>(
					new ApiResponseCustom(Instant.now(), 200, "OK", "Post not found", request.getRequestURI()),
					HttpStatus.OK);

		JwtUser jwtUser = userService.getAuthenticatedUser();
		Optional<User> author = userRepository.findByUsername(jwtUser.getUsername());
		if (!p.get().getAuthor().equals(author.get()))
			return new ResponseEntity<ApiResponseCustom>(new ApiResponseCustom(Instant.now(), 200, "OK",
					"You are not the owner of this post", request.getRequestURI()), HttpStatus.OK);

		p.get().setTags(tags.getTags());

		postRepository.save(p.get());

		return new ResponseEntity<ApiResponseCustom>(
				new ApiResponseCustom(Instant.now(), 200, "OK", "Tags added to post", request.getRequestURI()),
				HttpStatus.OK);

	}

	@PutMapping("private/remove-all-tags-from-post/{id}")
	@PreAuthorize("hasRole('EDITOR')")
	public ResponseEntity<ApiResponseCustom> removeAllTagsFromPost(@PathVariable Long id, HttpServletRequest request) {

		Optional<Post> p = postRepository.findById(id);
		if (!p.isPresent())
			return new ResponseEntity<ApiResponseCustom>(
					new ApiResponseCustom(Instant.now(), 200, "OK", "Post not found", request.getRequestURI()),
					HttpStatus.OK);

		JwtUser jwtUser = userService.getAuthenticatedUser();
		Optional<User> author = userRepository.findByUsername(jwtUser.getUsername());
		if (!p.get().getAuthor().equals(author.get()))
			return new ResponseEntity<ApiResponseCustom>(new ApiResponseCustom(Instant.now(), 200, "OK",
					"You are not the owner of this post", request.getRequestURI()), HttpStatus.OK);

		p.get().setTags(null);

		postRepository.save(p.get());

		return new ResponseEntity<ApiResponseCustom>(
				new ApiResponseCustom(Instant.now(), 200, "OK", "All tags have been removed", request.getRequestURI()),
				HttpStatus.OK);
	}

	@GetMapping("public/get-posts-by-tags")
	public ResponseEntity<ApiResponseCustom> getPostsByTags(@RequestBody StringWrapper strings,
			HttpServletRequest request) {

		if (strings.getStrings().isEmpty())
			return new ResponseEntity<ApiResponseCustom>(new ApiResponseCustom(Instant.now(), 200, "OK",
					"No tags present in request", request.getRequestURI()), HttpStatus.OK);

		Set<String> tagsToUppercase = strings.getStrings().stream().map(String::toUpperCase)
				.collect(Collectors.toSet());

		List<Tag> tagsToFind = tagRepository.findByTagNameIn(tagsToUppercase);
		if (tagsToFind.isEmpty())
			return new ResponseEntity<ApiResponseCustom>(
					new ApiResponseCustom(Instant.now(), 200, "OK", "No tags found", request.getRequestURI()),
					HttpStatus.OK);

		List<Post> ps = postRepository.findByTagsIn(tagsToFind);
		if (ps.isEmpty())
			return new ResponseEntity<ApiResponseCustom>(
					new ApiResponseCustom(Instant.now(), 200, "OK", "No posts found", request.getRequestURI()),
					HttpStatus.OK);

		Set<PostsHomeResponse> postsFound = ps.stream().map(PostsHomeResponse::createFromEntity)
				.collect(Collectors.toSet());

		return new ResponseEntity<ApiResponseCustom>(
				new ApiResponseCustom(Instant.now(), 200, "OK", postsFound, request.getRequestURI()), HttpStatus.OK);

	}

	@GetMapping("public/get-posts-ordered-by-avg")
	public ResponseEntity<ApiResponseCustom> getPostsOrderedByAvg(HttpServletRequest request) {

		List<Post> ps = postRepository.findAllByVisibleTrueOrderByAvgRatingDesc();
		if (ps.isEmpty())
			return new ResponseEntity<ApiResponseCustom>(
					new ApiResponseCustom(Instant.now(), 200, "OK", "No posts found", request.getRequestURI()),
					HttpStatus.OK);

		List<PostByAvgAndTotal> pas = ps.stream().map(PostByAvgAndTotal::createFromEntity).collect(Collectors.toList());

		// first way
		Instant start1 = Instant.now();
		pas.stream().forEach(p -> p.setTotalVote(
				ratingRepository.countByRatingUserPostCompositeKeyPost(postRepository.findById(p.getPostId()).get())));
		Instant end1 = Instant.now();

		// second way
		Instant start2 = Instant.now();
		List<Rating> r = ratingRepository.findAll();
		pas.stream().forEach(p -> p.setTotalVote(r.stream()
				.filter(c -> c.getRatingUserPostCompositeKey().getPost().getId().equals(p.getPostId())).count()));
		Instant end2 = Instant.now();

		log.info("--- FIRST WAY  -> COUNT STREAM JAVA + QUERIES --> " + Duration.between(start1, end1).toMillis());
		log.info("--- SECOND WAY -> COUNT STREAM JAVA + FILTER  --> " + Duration.between(start2, end2).toMillis());

		return new ResponseEntity<ApiResponseCustom>(
				new ApiResponseCustom(Instant.now(), 200, "OK", pas, request.getRequestURI()), HttpStatus.OK);

	}

	@GetMapping("private/create-pdf/{postId}")
	public ResponseEntity<?> downloadPDF(@PathVariable Long postId, HttpServletRequest request) {

		Optional<Post> p = postRepository.findById(postId);
		if (!p.isPresent())
			return new ResponseEntity<ApiResponseCustom>(
					new ApiResponseCustom(Instant.now(), 200, "OK", "Post not found", request.getRequestURI()),
					HttpStatus.OK);

		// POST:
		// title
		// image
		// content
		// author (username)
		// createdAt
		// rating

		// PDF LIBRARIES: PDF BOX, ITEXT

		InputStream pdfFile = null;
		ResponseEntity<InputStreamResource> response = null;
		try {
			pdfFile = pdfService.createPdfFromPost(p.get());
			// Set headers
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.parseMediaType("application/pdf"));
			headers.add("Access-Control-Allow-Origin", "*");
			headers.add("Access-Control-Allow-Methods", "GET");
			headers.add("Access-Control-Allow-Headers", "Content-Type");
			headers.add("Content-disposition", "inline; filename=" + p.get().getTitle().replaceAll(" ", "_") + ".pdf");
			headers.add("Cache-Control", "no-cache, no-store, must-revalidate");
			headers.add("Pragma", "no-cache");
			headers.add("Expires", "0");

			response = new ResponseEntity<InputStreamResource>(new InputStreamResource(pdfFile), headers,
					HttpStatus.OK);
		} catch (Exception e) {
			log.error("Some error occurs in pdf generation: " + e.getMessage());
			response = new ResponseEntity<InputStreamResource>(
					new InputStreamResource(null, "Some error occurs in pdf generation: " + e.getMessage()),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}

		return response;

	}
}
