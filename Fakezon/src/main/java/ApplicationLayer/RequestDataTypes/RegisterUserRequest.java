package ApplicationLayer.RequestDataTypes;

public class RegisterUserRequest {
    private String email;
    private String password;
    private String dateOfBirth;
    private String country;

    public RegisterUserRequest(String email, String password, String dateOfBirth, String country) {
        this.email = email;
        this.password = password;
        this.dateOfBirth = dateOfBirth;
        this.country = country;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(String dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }
}