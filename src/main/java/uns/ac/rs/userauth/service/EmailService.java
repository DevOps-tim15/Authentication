package uns.ac.rs.userauth.service;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import uns.ac.rs.userauth.domain.User;
import uns.ac.rs.userauth.domain.VerificationToken;


@Service
public class EmailService {

	@Autowired
	private JavaMailSender javaMailSender;
	
	@Autowired
	private Environment env;
	
	@Autowired
	private VerificationTokenService verificationService;

	@Async
	public void sendNotificaitionAsyncRegistration(User user) throws MailException, InterruptedException, UnsupportedEncodingException {
		System.out.println("VErification");
		String token = UUID.randomUUID().toString();
		VerificationToken verToken = new VerificationToken();
		verToken.setId(null);
		verToken.setToken(token);
		verToken.setUser(user);
		verificationService.saveToken(verToken);

		SimpleMailMessage mail = new SimpleMailMessage();
		mail.setTo(user.getEmail());
		mail.setFrom(env.getProperty("spring.mail.username"));
		mail.setSubject("Confirmation of registration");
		String tekst = null;
		tekst = String.format("Confirm your registration on this link: \nhttp://localhost:4200/#/registration/confirmation/%s",URLEncoder.encode(token, "UTF-8"));

		mail.setText(tekst);
		javaMailSender.send(mail);

		System.out.println("Email sent.");
	}
}
