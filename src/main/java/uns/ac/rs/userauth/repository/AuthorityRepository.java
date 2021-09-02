package uns.ac.rs.userauth.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import uns.ac.rs.userauth.domain.Authority;


public interface AuthorityRepository extends JpaRepository<Authority, Integer> {

}
