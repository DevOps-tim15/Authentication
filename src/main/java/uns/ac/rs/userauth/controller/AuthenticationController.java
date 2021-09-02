package uns.ac.rs.userauth.controller;

import java.io.UnsupportedEncodingException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.MailException;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import uns.ac.rs.userauth.domain.User;
import uns.ac.rs.userauth.dto.UserRegistrationDTO;
import uns.ac.rs.userauth.service.CustomUserDetailsService;
import uns.ac.rs.userauth.util.InvalidDataException;

@RestController
public class AuthenticationController {

	@Autowired
	private CustomUserDetailsService userDetailsService;
	
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
}
