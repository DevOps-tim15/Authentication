package uns.ac.rs.userauth.integration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.UnsupportedEncodingException;

import javax.transaction.Transactional;

import org.junit.Test;
import org.junit.jupiter.api.Order;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mail.MailException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import uns.ac.rs.userauth.domain.User;
import uns.ac.rs.userauth.domain.UserType;
import uns.ac.rs.userauth.domain.VerificationToken;
import uns.ac.rs.userauth.dto.UserRegistrationDTO;
import uns.ac.rs.userauth.repository.VerificationTokenRepository;
import uns.ac.rs.userauth.service.CustomUserDetailsService;
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
        User u = userService.saveRegisteredUser(dto);
        assertEquals(username, u.getUsername());
        assertTrue(passwordEncoder.matches(password, u.getPassword()));
        assertEquals(UserType.ROLE_REGISTERED_USER.toString(), u.getAuthorities().get(0).getUserType());
        assertFalse(u.isVerified());
        
        VerificationToken verificationToken = verificationTokenRepository.findByUser(u);
        boolean confirmed = userService.confirmRegistration(verificationToken.getToken());
        assertTrue(confirmed);
        User user = userService.findUserByToken(verificationToken.getToken());
        assertTrue(user.isVerified());
	}	
	
	@Test(expected = InvalidDataException.class)
	@Transactional
	@Order(2)
    public void testRegistration_missing_data() throws MailException, UnsupportedEncodingException, InterruptedException, InvalidDataException {
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

}
