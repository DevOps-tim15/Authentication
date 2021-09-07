package uns.ac.rs.userauth.kafka;

import java.io.UnsupportedEncodingException;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.mail.MailException;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import uns.ac.rs.userauth.domain.User;
import uns.ac.rs.userauth.kafka.domain.UserMessage;
import uns.ac.rs.userauth.security.TokenBasedAuthentication;
import uns.ac.rs.userauth.security.TokenUtils;
import uns.ac.rs.userauth.service.CustomUserDetailsService;

@Service
public class Consumer {
	@Autowired
	ObjectMapper objectMapper;
	
	@Autowired
	CustomUserDetailsService customUserDetailsService;
	
	@Autowired
	Producer producer;
	
	@Autowired
	private TokenUtils tokenUtils;

	@Autowired
	private UserDetailsService userDetailsService;

	
	@KafkaListener(topics="user-topic", groupId="mygroup")
	public void consumeFromTopic(ConsumerRecord<String, String> consumerRecord) throws JsonProcessingException, MailException, UnsupportedEncodingException, InterruptedException {
		String value = consumerRecord.value();
		System.out.println("Consummed message " + value);
		
		UserMessage message = null;
		try {
			message = objectMapper.readValue(value, UserMessage.class);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("Consumer has read message!");
		if(message.getType().equals("registration-rollback")) {
			customUserDetailsService.deleteUser(message.getUser());
		}
	}
	
    @SendTo
    @KafkaListener(topics = "logged-in", groupId = "mygroup")
    public String checkLoggedIn(String authToken) throws JsonProcessingException {
    	System.out.println("HI!");   
		String username = tokenUtils.getUsernameFromToken(authToken);
		String auth = null;
		if (username != null) {
			// uzmi user-a na osnovu username-a
			UserDetails userDetails = userDetailsService.loadUserByUsername(username);
			
			// proveri da li je prosledjeni token validan
			if (tokenUtils.validateToken(authToken, userDetails)) {
				// kreiraj autentifikaciju
				auth = username;
			}
		}
		return auth;
    }
}