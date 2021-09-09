package uns.ac.rs.userauth.dto;

public class UserRegistrationDTO {
	
	private String username;
	private String password;
	private String email;
	private String firstName;
	private String lastName;
	private String phone;
	private String websiteUrl;
	private String sex;
	private String birthDate;
	private String biography;
	private Boolean canBeTagged;
	private Boolean isPrivate;

	public UserRegistrationDTO() {
		super();
	}

	public UserRegistrationDTO(String username, String password, String email, String firstName, String lastName,
			String phone, String websiteUrl, String sex, String birthDate, String biography, Boolean canBeTagged,
			Boolean isPrivate) {
		super();
		this.username = username;
		this.password = password;
		this.email = email;
		this.firstName = firstName;
		this.lastName = lastName;
		this.phone = phone;
		this.websiteUrl = websiteUrl;
		this.sex = sex;
		this.birthDate = birthDate;
		this.biography = biography;
		this.canBeTagged = canBeTagged;
		this.isPrivate = isPrivate;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public String getWebsiteUrl() {
		return websiteUrl;
	}

	public void setWebsiteUrl(String websiteUrl) {
		this.websiteUrl = websiteUrl;
	}

	public String getSex() {
		return sex;
	}

	public void setSex(String sex) {
		this.sex = sex;
	}

	public String getBirthDate() {
		return birthDate;
	}

	public void setBirthDate(String birthDate) {
		this.birthDate = birthDate;
	}

	public String getBiography() {
		return biography;
	}

	public void setBiography(String biography) {
		this.biography = biography;
	}

	public Boolean getCanBeTagged() {
		return canBeTagged;
	}

	public void setCanBeTagged(Boolean canBeTagged) {
		this.canBeTagged = canBeTagged;
	}

	public Boolean getIsPrivate() {
		return isPrivate;
	}

	public void setIsPrivate(Boolean isPrivate) {
		this.isPrivate = isPrivate;
	}

	@Override
	public String toString() {
		return "UserRegistrationDTO [username=" + username + ", password=" + password + ", email=" + email
				+ ", firstName=" + firstName + ", lastName=" + lastName + ", phone=" + phone + ", websiteUrl="
				+ websiteUrl + ", sex=" + sex + ", birthDate=" + birthDate + ", biography=" + biography
				+ ", canBeTagged=" + canBeTagged + ", isPrivate=" + isPrivate + "]";
	}

}