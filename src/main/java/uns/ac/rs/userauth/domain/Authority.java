package uns.ac.rs.userauth.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import org.springframework.security.core.GrantedAuthority;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name="Authorities")
public class Authority implements GrantedAuthority {

	private static final long serialVersionUID = 1L;

	@Id
    @Column(name="id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

	@Column(name = "user_type")
	@Enumerated(EnumType.STRING)
	private UserType userType;

    public String getAuthority() {
        return userType.toString();
    }

    public void setUserType(UserType type) {
        this.userType = type;
    }

    @JsonIgnore
    public String getUserType() {
        return userType.toString();
    }

    @JsonIgnore
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

	@Override
	public String toString() {
		return "Authority [id=" + id + ", userType=" + userType + "]";
	}
}
