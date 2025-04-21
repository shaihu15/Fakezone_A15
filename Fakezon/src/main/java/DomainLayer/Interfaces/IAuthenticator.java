package DomainLayer.Interfaces;

public interface IAuthenticator {
    String register(int userId);

    String login(int userId);

    void logout(int userId);

    boolean isValid(String sessionToken);
}
