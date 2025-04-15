package DomainLayer.Interfaces;

public interface IAuthenticator {
    boolean register(String email, String password);
    boolean login(String email, String password);
    void logout(String email);
}
