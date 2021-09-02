package uns.ac.rs.userauth.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import uns.ac.rs.userauth.domain.VerificationToken;
import uns.ac.rs.userauth.repository.VerificationTokenRepository;

@Service
public class VerificationTokenService {
	@Autowired
	private VerificationTokenRepository verificationTokenRepository;

	public void saveToken(VerificationToken token) {
		verificationTokenRepository.save(token);
	}
}
