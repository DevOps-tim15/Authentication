package uns.ac.rs.userauth.kafka;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import uns.ac.rs.userauth.kafka.domain.UserMessage;

@Service
public class Producer {

	  @Autowired 
	  private KafkaTemplate<String, String> kafkaTemp;
	  
	//  @Autowired 
	//  private ReplyingKafkaTemplate<String, String, String> rkafkaTemp;
	  
	  @Autowired
	  private ObjectMapper objectMapper;
	  
	  @Async
	  public void sendMessageToTopic(String topic, UserMessage userMssage) throws JsonProcessingException {
			String value = objectMapper.writeValueAsString(userMssage);
			kafkaTemp.send(topic ,value);
			System.out.println("Publishing to topic "+topic);
	  }
}
