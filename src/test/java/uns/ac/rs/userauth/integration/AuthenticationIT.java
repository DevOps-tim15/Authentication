package uns.ac.rs.userauth.integration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;

import java.io.UnsupportedEncodingException;

import javax.transaction.Transactional;

import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.Order;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.MailException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import com.fasterxml.jackson.core.JsonProcessingException;

import uns.ac.rs.userauth.domain.User;
import uns.ac.rs.userauth.domain.UserType;
import uns.ac.rs.userauth.domain.VerificationToken;
import uns.ac.rs.userauth.dto.UserRegistrationDTO;
import uns.ac.rs.userauth.kafka.Producer;
import uns.ac.rs.userauth.kafka.domain.UserMessage;
import uns.ac.rs.userauth.repository.VerificationTokenRepository;
import uns.ac.rs.userauth.security.JwtAuthenticationRequest;
import uns.ac.rs.userauth.service.CustomUserDetailsService;
import uns.ac.rs.userauth.service.EmailService;
import uns.ac.rs.userauth.util.InvalidDataException;

@ActiveProfiles("test")
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Transactional
public class AuthenticationIT {

    @Autowired
    private CustomUserDetailsService userService;

    @Autowired
    private VerificationTokenRepository verificationTokenRepository;
    
	@Autowired
	private PasswordEncoder passwordEncoder;
	
	@Autowired
    private TestRestTemplate testRestTemplate;
	
	@Autowired
	private AuthenticationManager authenticationManager;
	
	@MockBean
	private Producer producer;
	
	@MockBean
	private EmailService emailService;
	
	String token;
	
	@Test
	@Transactional
	@Order(1)
	public void userRegistration_successfully() throws Exception {
		UserRegistrationDTO dto = new UserRegistrationDTO();
		String username = "pera";
		String password = "pera123";
		dto.setFirstName("Pera");
		dto.setLastName("Peric");
		dto.setUsername(username);
		dto.setPassword(password);
		dto.setEmail("pera@gmail.com");
		
		EmailService spyEmail = Mockito.spy(emailService);
		Mockito.doNothing().when(spyEmail).sendNotificaitionAsyncRegistration(any(User.class), any(String.class), any(String.class));
		
        User u = userService.saveRegisteredUser(dto);
        assertEquals(username, u.getUsername());
        assertTrue(passwordEncoder.matches(password, u.getPassword()));
        assertEquals(UserType.ROLE_REGISTERED_USER.toString(), u.getAuthorities().get(0).getUserType());
        assertFalse(u.isVerified());
//        
//		Producer spy = Mockito.spy(producer);
//		Mockito.doNothing().when(spy).sendMessageToTopic(any(String.class), any(UserMessage.class));
        Mockito.doNothing().when(producer).sendMessageToTopic(any(String.class), any(UserMessage.class));
        
        VerificationToken verificationToken = verificationTokenRepository.findByUser(u);
        boolean confirmed = userService.confirmRegistration(verificationToken.getToken());
        assertTrue(confirmed);
        User user = userService.findUserByToken(verificationToken.getToken());
        assertTrue(user.isVerified());
	}	
	
	@Test(expected = InvalidDataException.class)
	@Transactional
	@Order(2)
    public void testRegistration_missing_data() throws MailException, UnsupportedEncodingException, InterruptedException, InvalidDataException, JsonProcessingException {
		UserRegistrationDTO dto = new UserRegistrationDTO();
		
		dto.setFirstName("Pera");
		dto.setLastName("Peric");
		dto.setUsername("pera_novi");
		dto.setPassword("pera123");
		dto.setEmail("");
		try {
			userService.saveRegisteredUser(dto);
		} catch (InvalidDataException e) {
			assertEquals("User information is incomplete!", e.getMessage());
			throw e;
		}
    }
	
	@Test
	@Transactional
	@Order(3)
	public void loginTest_successfully() throws Exception {
		JwtAuthenticationRequest req = new JwtAuthenticationRequest("jova", "123");
		HttpEntity<JwtAuthenticationRequest> httpEntity = new HttpEntity<JwtAuthenticationRequest>(req);
		String url = "/login";
		ResponseEntity<String> responseEntity = testRestTemplate.exchange(url, HttpMethod.POST, httpEntity, String.class);
		assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
		String token = responseEntity.getBody();
		assertNotNull(token);
	}
	
	@Test
	@Transactional
	@Order(4)
	public void loginTest_wrong_password() throws Exception {
		JwtAuthenticationRequest req = new JwtAuthenticationRequest("jova", "wrong-pass");
		HttpEntity<JwtAuthenticationRequest> httpEntity = new HttpEntity<JwtAuthenticationRequest>(req);
		String url = "/login";
		ResponseEntity<String> response = testRestTemplate.exchange(url, HttpMethod.POST, httpEntity, String.class);
		String message = response.getBody();
		assertEquals("Wrong password!", message);
		assertEquals(HttpStatus.NOT_ACCEPTABLE, response.getStatusCode());
	}
	
	@Test
	@Transactional
	@Order(5)
	public void updateProfile_successfully() throws JsonProcessingException {
		JwtAuthenticationRequest loginDto = new JwtAuthenticationRequest("jova", "123");
        ResponseEntity<String> loginResponse = testRestTemplate.postForEntity("/login", loginDto, String.class);
        token = loginResponse.getBody();
        
        UserRegistrationDTO dto = new UserRegistrationDTO();
		dto.setUsername("jova");
		dto.setFirstName("Pera");
		dto.setLastName("Peric");
		dto.setBiography("New bio");
		dto.setEmail("noviuser@gmail.com");
		
//		Producer spy = Mockito.spy(producer);
//		Mockito.doNothing().when(spy).sendMessageToTopic(any(String.class), any(UserMessage.class));
		
        Mockito.doNothing().when(producer).sendMessageToTopic(any(String.class), any(UserMessage.class));

		HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + token);
		HttpEntity<UserRegistrationDTO> httpEntity = new HttpEntity<UserRegistrationDTO>(dto,  headers);
		String url = "/update";
		ResponseEntity<String> response = testRestTemplate.exchange(url, HttpMethod.PUT, httpEntity, String.class);
		String message = response.getBody();
		assertEquals("noToken", message);
		assertEquals(HttpStatus.OK, response.getStatusCode());
	}
	
	@Test
	@Transactional
	@Order(6)
	public void updateProfile_no_username() {
		JwtAuthenticationRequest loginDto = new JwtAuthenticationRequest("jova", "123");
        ResponseEntity<String> loginResponse = testRestTemplate.postForEntity("/login", loginDto, String.class);
        token = loginResponse.getBody();
        
        UserRegistrationDTO dto = new UserRegistrationDTO();
		dto.setFirstName("Pera");
		dto.setLastName("Peric");
		dto.setBiography("New bio");
		dto.setEmail("pera@gmail.com");
		
		HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + token);
		HttpEntity<UserRegistrationDTO> httpEntity = new HttpEntity<UserRegistrationDTO>(dto,  headers);
		String url = "/update";
		ResponseEntity<String> response = testRestTemplate.exchange(url, HttpMethod.PUT, httpEntity, String.class);
		String message = response.getBody();
		assertEquals("Some data is missing", message);
		assertEquals(HttpStatus.NOT_ACCEPTABLE, response.getStatusCode());
	}
}
