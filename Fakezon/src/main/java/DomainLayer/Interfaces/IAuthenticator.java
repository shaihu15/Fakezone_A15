package DomainLayer.Interfaces;

import java.time.LocalDate;

import ApplicationLayer.Response;

public interface IAuthenticator {
    Response<String> register(String email, String password, LocalDate dateOfBirth, String country);

    String login(String email, String password);

    void logout(String email);

    boolean isValid(String sessionToken);

    String getEmail(String sessionToken);

    int getUserId(String sessionToken);

    String generateGuestToken();

    Boolean isGuestToken(String token);
}
