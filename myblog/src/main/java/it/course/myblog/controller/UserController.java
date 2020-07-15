package it.course.myblog.controller;

import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.transaction.Transactional;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;

import it.course.myblog.entity.User;
import it.course.myblog.entity.UserReported;
import it.course.myblog.entity.UserReportedId;
import it.course.myblog.payload.response.ApiResponseCustom;
import it.course.myblog.payload.response.UserMeResponse;
import it.course.myblog.payload.response.UserResponse;
import it.course.myblog.repository.UserRepository;
import it.course.myblog.repository.AuthorityRepository;
import it.course.myblog.repository.UserReportedRepository;
import it.course.myblog.security.JwtTokenUtil;
import it.course.myblog.security.JwtUser;
import it.course.myblog.service.UserService;
import javassist.expr.NewArray;
import lombok.extern.slf4j.Slf4j;
import it.course.myblog.config.ApiValidationExceptionHandler;
import it.course.myblog.entity.Authority;
import it.course.myblog.entity.AuthorityName;
import it.course.myblog.exception.AppException;
import it.course.myblog.payload.request.SignUpRequest;
import it.course.myblog.payload.request.UpdateMe;
import it.course.myblog.payload.request.ChangeAuthority;
import it.course.myblog.payload.request.SignInRequest;
import it.course.myblog.security.JwtAuthenticationResponse;

@Slf4j
@RestController
public class UserController {
	
	@Value("${jwt.header}")
    private String tokenHeader;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private UserReportedRepository userReportedRepository;
    
    @Autowired
    AuthorityRepository authorityRepository;

    @Autowired
    private UserDetailsService userDetailsService;
    
    @Autowired
	PasswordEncoder passwordEncoder;
    
    @Autowired
	UserService userService;
	
	
	//GET Users List - select * from user;
	@GetMapping("private/get-users")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<ApiResponseCustom> getAllUsers(HttpServletRequest request){
		
		List<User> us = userRepository.findAllByEnabledTrue();
		
		if(us.isEmpty())
			return new ResponseEntity<ApiResponseCustom>(new ApiResponseCustom (
					Instant.now(), 200, "OK", "Users not found", request.getRequestURI()), HttpStatus.OK);
		
		List<UserResponse> urs = us.stream().map(UserResponse::createFromEntity).collect(Collectors.toList());
		
		return new ResponseEntity<ApiResponseCustom>(new ApiResponseCustom (
				Instant.now(), 200, "OK", urs, request.getRequestURI())	, HttpStatus.OK);	
		
	}
	
	//GET Single Users - select * from user where id = ?;
	@GetMapping("private/get-user")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<ApiResponseCustom> getSingleUser(@RequestParam Long id, HttpServletRequest request){
		
		Optional<User> u = userRepository.findByIdAndEnabledTrue(id);
	
		if(u.isPresent()) {
			UserResponse ur = UserResponse.createFromEntity(u.get());
			return new ResponseEntity<ApiResponseCustom>(new ApiResponseCustom (
					Instant.now(), 200, "OK", ur, request.getRequestURI())	, HttpStatus.OK);
		}
			
		return new ResponseEntity<ApiResponseCustom>(new ApiResponseCustom (
				Instant.now(), 200, "OK", "User not found", request.getRequestURI()), HttpStatus.OK);
		
	}
	
	//GET ME - select * from user where id = ?;
	@GetMapping("private/get-me")
	public ResponseEntity<ApiResponseCustom> getMe(HttpServletRequest request){
		
		JwtUser jwtUser = userService.getAuthenticatedUser();
		
		Optional<User> u = userRepository.findByUsername(jwtUser.getUsername());
		
		UserMeResponse um = UserMeResponse.createFromEntity(u.get());
	
		if(u.isPresent()) {
			return new ResponseEntity<ApiResponseCustom>(new ApiResponseCustom (
					Instant.now(), 200, "OK", um, request.getRequestURI())	, HttpStatus.OK);
		}
			
		return new ResponseEntity<ApiResponseCustom>(new ApiResponseCustom (
				Instant.now(), 200, "OK", "User not found", request.getRequestURI()), HttpStatus.OK);
		
	}
	
	
	//POST - insert into user ...
//	@PostMapping("/insert-user")
//	public ResponseEntity<ApiResponseCustom> insertUser(@Valid @RequestBody User user, HttpServletRequest request) {
//		
//		if(userRepository.existsByUsernameOrEmail(user.getUsername(), user.getEmail()))
//			return new ResponseEntity<ApiResponseCustom>(new ApiResponseCustom (
//					Instant.now(), 200, "OK", "Username or Email already in use", request.getRequestURI()), HttpStatus.OK);
//		
//		User u = new User();
//		u.setEmail(user.getEmail());
//		u.setUsername(user.getUsername());
//		u.setPassword(user.getPassword());
//		//u.setEnabled(true);
//		userRepository.save(u);
//		return new ResponseEntity<ApiResponseCustom>(new ApiResponseCustom (
//				Instant.now(), 200, "OK", "User susccesfully created", request.getRequestURI()), HttpStatus.OK);
//		
//	}
	
	
	// UPDATE ME
	@PutMapping("private/update-me")
	public  ResponseEntity<ApiResponseCustom> updateMe(@Valid @RequestBody UpdateMe updateMe, HttpServletRequest request) {
		
		JwtUser jwtUser = userService.getAuthenticatedUser();
		
		Optional<User> u = userRepository.findByUsername(jwtUser.getUsername());
		
		if(u.isPresent()) {		
			
			if(!updateMe.getPassword().equals(u.get().getPassword()))
				u.get().setPassword(passwordEncoder.encode(updateMe.getPassword()));
			
			u.get().setUsername(updateMe.getUsername());
			
			userRepository.save(u.get());
			return new ResponseEntity<ApiResponseCustom>(new ApiResponseCustom (
					Instant.now(), 200, "OK", "User susccesfully updated", request.getRequestURI()), HttpStatus.OK);			
		} else {
			return new ResponseEntity<ApiResponseCustom>(new ApiResponseCustom (
					Instant.now(), 200, "OK", "User not found", request.getRequestURI()), HttpStatus.OK);
		}
		
	}
	
	
	// LOGICAL DELETE
	@PutMapping("private/enable-disable-user/{id}")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<ApiResponseCustom> enableDisableUser(@PathVariable Long id, HttpServletRequest request)  {
		
		Optional<User> u = userRepository.findById(id);
		
		if(u.isPresent()) {
			u.get().setEnabled(!u.get().getEnabled());
			userRepository.save(u.get());
			String s = u.get().getEnabled() ? "enabled":"disabled";
			return new ResponseEntity<ApiResponseCustom>(new ApiResponseCustom (
					Instant.now(), 200, "OK", "User susccesfully "+s, request.getRequestURI()), HttpStatus.OK);	
		} else {
			return new ResponseEntity<ApiResponseCustom>(new ApiResponseCustom (
					Instant.now(), 200, "OK", "User not found", request.getRequestURI()), HttpStatus.OK);
		}
		
	}
	
	@PostMapping(value = "public/signin")
    public ResponseEntity<?> createAuthenticationToken(@RequestBody SignInRequest authenticationRequest, HttpServletResponse response, HttpServletRequest request) throws AuthenticationException, JsonProcessingException {

		//Enable user after 1 week post ban
		Optional<User> u = userRepository.findByUsername(authenticationRequest.getUsername());
		if(u.isPresent()) {
			Optional<UserReported> ur = userReportedRepository.findByUserReportedId(new UserReportedId(u.get()));
			if(ur.isPresent() && ur.get().getReportNumber() > 1) {
				// date NOW
				Date today = new Date(System.currentTimeMillis());
				//  difference between today and last update date on reported_user table (in days)
				long diff = TimeUnit.DAYS.convert(Math.abs(today.getTime() - ur.get().getUpdatedAt().getTime()), TimeUnit.MILLISECONDS);
				if(diff > 7) {
					u.get().setEnabled(true);
					userRepository.save(u.get());
					userReportedRepository.delete(ur.get());
				}
			}
		}
		
        // Effettuo l'autenticazione
        final Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        authenticationRequest.getUsername(),
                        authenticationRequest.getPassword()
                )
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Genero Token
        final UserDetails userDetails = userDetailsService.loadUserByUsername(authenticationRequest.getUsername());
        final String token = jwtTokenUtil.generateToken(userDetails);
        
        //Per aggiungere il token nell'header basta scommentare la riga sotto
        //response.setHeader(tokenHeader,token);
        
        // Ritorno il token
        return new ResponseEntity<ApiResponseCustom>(new ApiResponseCustom(Instant.now(), 200, null, 
        		new JwtAuthenticationResponse(
        				userDetails.getUsername(),
        				userDetails.getAuthorities(), 
        				token), 
        		request.getRequestURI()), HttpStatus.OK );
		
    }
	
	@PostMapping("public/signup")
	@Transactional
	public ResponseEntity<ApiResponseCustom> registerUser(@Valid @RequestBody SignUpRequest signUpRequest, HttpServletRequest request) {
		
		log.info("Call controller registerUser with SignUpRequest as parameter: {}, {}", signUpRequest.getEmail(), signUpRequest.getUsername());
		
		if(userRepository.existsByUsername(signUpRequest.getUsername())) {
			log.info("Username {} already in use", signUpRequest.getUsername());
			return new ResponseEntity<ApiResponseCustom>(new ApiResponseCustom( Instant.now(), 403, null, "Username already in use !", request.getRequestURI()), HttpStatus.BAD_REQUEST);	
		}
		
		if(userRepository.existsByEmail(signUpRequest.getEmail())) {
			log.info("Email {} already in use", signUpRequest.getEmail());
			return new ResponseEntity<ApiResponseCustom>(new ApiResponseCustom( Instant.now(), 403, null, "Email already in use !", request.getRequestURI()), HttpStatus.BAD_REQUEST);
		}
		
		User user = new User(signUpRequest.getEmail(), signUpRequest.getUsername(),  signUpRequest.getPassword());
		
		user.setPassword(passwordEncoder.encode(user.getPassword()));
		userRepository.save(user);
		
		Authority userAuthority = authorityRepository.findByName(AuthorityName.ROLE_READER)
        .orElseThrow(() -> new AppException("User Authority not set."));

		user.setAuthorities(Collections.singleton(userAuthority));
		
		log.info("User creation successfully completed", signUpRequest.getEmail());
		return new ResponseEntity<ApiResponseCustom>(new ApiResponseCustom(Instant.now(), 200, null, "User creation successfully completed", request.getRequestURI()), HttpStatus.OK );
		
	}
	
	@PutMapping("private/update-authority")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<ApiResponseCustom> updateAuthority(@Valid @RequestBody ChangeAuthority changeAuthority, HttpServletRequest request) throws EnumConstantNotPresentException{
		
		Optional<User> u = userRepository.findByUsername(changeAuthority.getUsername());
		
		if(!u.isPresent())
		return new ResponseEntity<ApiResponseCustom>(new ApiResponseCustom (
				Instant.now(), 200, "OK", "User not found", request.getRequestURI()), HttpStatus.OK);
		
		Set<AuthorityName> authorityNames = changeAuthority.getAuthorityNames();
		
		if(authorityNames.isEmpty())
			return new ResponseEntity<ApiResponseCustom>(new ApiResponseCustom (
					Instant.now(), 200, "OK", "No authorities have been selected", request.getRequestURI()), HttpStatus.OK);
		
		Set<Authority> authorities = authorityRepository.findByNameIn(authorityNames);
		
		u.get().setAuthorities(authorities);
		
		userRepository.save(u.get());		
		
		return new ResponseEntity<ApiResponseCustom>(new ApiResponseCustom(Instant.now(), 200, null, "Authorities have been updated", request.getRequestURI()), HttpStatus.OK );
		
	}
	
	/*
	 * @PutMapping("private/change-password-by-logged-user") public
	 * ResponseEntity<ApiResponseCustom> changePasswordByLoggedUser(@RequestParam
	 * String newPassword, HttpServletRequest request){
	 * 
	 * JwtUser jwtUser = userService.getAuthenticatedUser();
	 * 
	 * Optional<User> u = userRepository.findByUsername(jwtUser.getUsername());
	 * 
	 * u.get().setPassword(passwordEncoder.encode(newPassword));
	 * 
	 * userRepository.save(u.get());
	 * 
	 * return new ResponseEntity<ApiResponseCustom>(new
	 * ApiResponseCustom(Instant.now(), 200, null, "The password has been updated",
	 * request.getRequestURI()), HttpStatus.OK );
	 * 
	 * }
	 */
	
	@PutMapping("public/forgot-password")
	public ResponseEntity<ApiResponseCustom> forgotPassword(@RequestParam String usernameOrEmail, HttpServletRequest request){
		
		Optional<User> u = userRepository.findByUsernameOrEmail(usernameOrEmail, usernameOrEmail);
		
		if(!u.isPresent())
			return new ResponseEntity<ApiResponseCustom>(new ApiResponseCustom (
					Instant.now(), 200, "OK", "User not found", request.getRequestURI()), HttpStatus.OK);
		
		/*
		if(u.get().getIdentifierCode() != null)
			return new ResponseEntity<ApiResponseCustom>(new ApiResponseCustom (
					Instant.now(), 200, "OK", "You have already requested a change password", request.getRequestURI()), HttpStatus.OK);
		*/		
		
		String identifierCode = null;
		
		try {
			identifierCode = UserService.toHexString(UserService.getSHA(Instant.now().toString()));
			u.get().setIdentifierCode(identifierCode);
			userRepository.save(u.get());
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return new ResponseEntity<ApiResponseCustom>(new ApiResponseCustom (
				Instant.now(), 200, "OK", "Please, check your email in order to reset the password: http://localhost:8081/public/reset-password/"+identifierCode, request.getRequestURI()), HttpStatus.OK);
	}
	
	@PutMapping("public/reset-password/{identifierCode}")
	public ResponseEntity<ApiResponseCustom> resetPassword(@PathVariable String identifierCode, @RequestParam String newPassword, HttpServletRequest request){
		
		Optional<User> u = userRepository.findByIdentifierCode(identifierCode);
		
		if(!u.isPresent())
			return new ResponseEntity<ApiResponseCustom>(new ApiResponseCustom (
					Instant.now(), 200, "OK", "Identifier code not found", request.getRequestURI()), HttpStatus.OK);
		
		u.get().setPassword(passwordEncoder.encode(newPassword));
		u.get().setIdentifierCode(null);
		userRepository.save(u.get());
		
		return new ResponseEntity<ApiResponseCustom>(new ApiResponseCustom (
				Instant.now(), 200, "OK", "Password has been modified", request.getRequestURI()), HttpStatus.OK);
	}
	
	
	@PutMapping("private/add-remove-follower")
	@PreAuthorize("hasRole('EDITOR') or hasRole('READER')")
	public ResponseEntity<ApiResponseCustom> addRemoveFollower(@RequestParam String followedUsername, HttpServletRequest request){
		
		JwtUser jwtUser = userService.getAuthenticatedUser();
		Optional<User> follower = userRepository.findByUsername(jwtUser.getUsername());
		if(!follower.isPresent())
			return new ResponseEntity<ApiResponseCustom>(new ApiResponseCustom (
					Instant.now(), 200, "OK", "Follower not found", request.getRequestURI()), HttpStatus.OK);
		
		
		Optional<User> followed = userRepository.findByUsernameAndEnabledTrue(followedUsername);
		if(!followed.isPresent())
			return new ResponseEntity<ApiResponseCustom>(new ApiResponseCustom (
					Instant.now(), 200, "OK", "Followed not found", request.getRequestURI()), HttpStatus.OK);
		/*
		if(!followed.get().getAuthorities().contains(authorityRepository.findByName(AuthorityName.valueOf("ROLE_EDITOR")).get())
				|| !followed.get().getAuthorities().contains(authorityRepository.findByName(AuthorityName.valueOf("ROLE_READER")).get()))
			return new ResponseEntity<ApiResponseCustom>(new ApiResponseCustom (
					Instant.now(), 200, "OK", "You cannot follow an ADMIN", request.getRequestURI()), HttpStatus.OK);		
		*/
		if(followed.get().getAuthorities().contains(authorityRepository.findByName(AuthorityName.valueOf("ROLE_ADMIN")).get())
				&& followed.get().getAuthorities().size() < 2)
			return new ResponseEntity<ApiResponseCustom>(new ApiResponseCustom (
					Instant.now(), 200, "OK", "You cannot follow an ADMIN", request.getRequestURI()), HttpStatus.OK);		
		
		if(follower.get().getEmail().equals(followed.get().getEmail()))
			return new ResponseEntity<ApiResponseCustom>(new ApiResponseCustom (
					Instant.now(), 200, "OK", "You can't follow yourself", request.getRequestURI()), HttpStatus.OK);
		
		Set<User> followers = follower.get().getFollowers();
		
		//String s = followers.contains(followed.get()) ? "added":"removed"; 
		String s = "added";
		if(followers.contains(followed.get())) {
			followers.remove(followed.get());
			s = "removed";
		}else {
			followers.add(followed.get());
		}
		
		follower.get().setFollowers(followers);
		userRepository.save(follower.get());
		
		return new ResponseEntity<ApiResponseCustom>(new ApiResponseCustom (
				Instant.now(), 200, "OK", "New followed "+s, request.getRequestURI()), HttpStatus.OK);
		
	}
	
	
	
	
	
	
}
