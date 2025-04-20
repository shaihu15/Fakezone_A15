package ApplicationLayer.DTO;

import com.fasterxml.jackson.annotation.JsonProperty;

public class UserDTO {
    @JsonProperty("userId")
    private final int userId;

    @JsonProperty("userEmail")
    private final String userEmail;

    @JsonProperty("userAge")
    private final int userAge;

    public UserDTO(int userId, String userEmail, int userAge) {
        this.userId = userId;
        this.userEmail = userEmail;
        this.userAge = userAge;
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
}
