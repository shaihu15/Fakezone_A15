package DomainLayer.Interfaces;

import java.time.LocalDate;

public interface IAuthenticator {
    String register(String email, String password, LocalDate dateOfBirth);

    String login(String email, String password);

    void logout(String email);

    boolean isValid(String sessionToken);
}
