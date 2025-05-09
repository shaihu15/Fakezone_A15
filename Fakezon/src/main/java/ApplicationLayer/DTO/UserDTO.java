package ApplicationLayer.DTO;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class UserDTO {

    @JsonProperty("userId")
    private  int userId;

    @JsonProperty("userEmail")
    private  String userEmail;

    @JsonProperty("userAge")
    private  int userAge;

    @JsonCreator
    public UserDTO(
            @JsonProperty("userId") int userId,
            @JsonProperty("userEmail") String userEmail,
            @JsonProperty("userAge") int userAge
    ) {
        this.userId = userId;
        this.userEmail = userEmail;
        this.userAge = userAge;
    }
    public UserDTO() {
        this.userId = 0;         // default values
        this.userEmail = null;
        this.userAge = 0;
    }

    public int getUserId() {
        return userId;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public int getUserAge() {
        return userAge;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }
    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }
    public void setUserAge(int userAge) {
        this.userAge = userAge;
    }
}
