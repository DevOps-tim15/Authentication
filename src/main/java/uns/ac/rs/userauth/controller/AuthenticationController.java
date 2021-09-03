package uns.ac.rs.userauth.controller;

import java.io.UnsupportedEncodingException;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.MailException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import uns.ac.rs.userauth.domain.User;
import uns.ac.rs.userauth.dto.UserRegistrationDTO;
import uns.ac.rs.userauth.security.JwtAuthenticationRequest;
import uns.ac.rs.userauth.security.TokenUtils;
import uns.ac.rs.userauth.service.CustomUserDetailsService;
import uns.ac.rs.userauth.util.InvalidDataException;

@RestController
@CrossOrigin(origins = "http://localhost:4200")
public class AuthenticationController {

	@Autowired
	private CustomUserDetailsService userDetailsService;
	
	@Autowired
	private TokenUtils tokenUtils;
	
	@Autowired
	private AuthenticationManager authenticationManager;
	
	@PostMapping(value = "/registration")
	public ResponseEntity<?> register(@RequestBody UserRegistrationDTO user) {
		try {
			System.out.println(user);
			return new ResponseEntity<User>(userDetailsService.saveRegisteredUser(user), HttpStatus.CREATED);
		} catch (InvalidDataException e) {
			return new ResponseEntity<String>(e.getMessage(), HttpStatus.NOT_ACCEPTABLE);
		} catch ( MailException | UnsupportedEncodingException |  InterruptedException e ) {		
			return new ResponseEntity<String>("Error while sending e-mail. Check to see if you entered it right!",HttpStatus.BAD_REQUEST);
		}
	}
	
	@PostMapping(value = "/confirm/{token}")
	public ResponseEntity<?> confirmRegistration(@PathVariable String token) {		
		try {
			return new ResponseEntity<Boolean>(userDetailsService.confirmRegistration(token), HttpStatus.CREATED);
		}
		catch(InvalidDataException e) {
			return new ResponseEntity<String>(e.getMessage(), HttpStatus.NOT_ACCEPTABLE);
		}
	}
	
	@PostMapping(value = "/login")
	public ResponseEntity<String> createAuthenticationToken(@RequestBody JwtAuthenticationRequest authenticationRequest,
			HttpServletResponse response) throws AuthenticationException{

		final Authentication authentication;
		try {
			this.userDetailsService.loadUserByUsername(authenticationRequest.getUsername());
			authentication = authenticationManager
				.authenticate(new UsernamePasswordAuthenticationToken(authenticationRequest.getUsername(),
						authenticationRequest.getPassword()));
		}
		catch(UsernameNotFoundException e) {
			return new ResponseEntity<>(e.getMessage(),HttpStatus.NOT_ACCEPTABLE);
		}
		catch(BadCredentialsException e) {
			return new ResponseEntity<>("Wrong password!",HttpStatus.NOT_ACCEPTABLE);
		}
		SecurityContextHolder.getContext().setAuthentication(authentication);

		User user = (User) authentication.getPrincipal();
		if(!user.isVerified()) {
			return new ResponseEntity<>("Not verified! See your email for verification.",HttpStatus.NOT_ACCEPTABLE);
		}
		String jwt = tokenUtils.generateToken(user.getUsername(), user.getAuthorities().get(0).getUserType());
		return new ResponseEntity<>(jwt, HttpStatus.OK);
	}
	
	@PostMapping(value = "/signout", produces="text/plain")
    public ResponseEntity<String> logoutUser() {
		try {
        	SecurityContextHolder.clearContext();
            return new ResponseEntity<>("You have successfully logged out!", HttpStatus.OK);
        }catch (Exception e) {
			return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
		}

    }
	
    @PreAuthorize("hasRole('ROLE_REGISTERED_USER')")
	@GetMapping( value = "/verify-registered-user")
    public ResponseEntity<String> verifyRegisteredUser() {
        return new ResponseEntity<>("Hello registered user!", HttpStatus.OK);
    }
}
