package uns.ac.rs.userauth.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import uns.ac.rs.userauth.domain.User;


public interface UserRepository extends JpaRepository<User, Long> {
	User findByUsername(String username);
	User findByEmail(String email);
	User findByUsernameAndPassword(String username, String password);
	
	@Query(value = "select * from user_t u where u.id =(select t.user_id from verification_tokens t where t.token = ?1)", nativeQuery = true)
	User findByToken(String token);
}
