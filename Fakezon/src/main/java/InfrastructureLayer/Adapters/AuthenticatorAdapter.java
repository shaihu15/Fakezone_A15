package InfrastructureLayer.Adapters;

import java.time.LocalDate;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ApplicationLayer.DTO.UserDTO;
import ApplicationLayer.Interfaces.IUserService;
import DomainLayer.Interfaces.IAuthenticator;
import DomainLayer.Model.Registered;
import InfrastructureLayer.Security.TokenService;

public class AuthenticatorAdapter implements IAuthenticator {
    private TokenService tokenService;
    private IUserService userService;
    private static final Logger logger = LoggerFactory.getLogger(AuthenticatorAdapter.class);

    public AuthenticatorAdapter(IUserService userService) {
        this.tokenService = new TokenService();
        this.userService = userService;
    }

    @Override
    public String register(String email, String password, LocalDate dateOfBirth, String country) {
        try {
            logger.info("Registering user with email: {}", email);
            UserDTO userDTO = userService.registerUser(email, password, dateOfBirth, country);
            if (userDTO != null) {
                logger.info("User registered successfully: {}", userDTO.getUserEmail());
                // Generate a token for the registered user with userId
                String token = tokenService.generateToken(userDTO.getUserEmail(), userDTO.getUserId());
                return token;
            } else {
                logger.error("User registration failed for email: {}", email);
                return null; // Registration failed
            }
        } catch (Exception e) {
            logger.error("Error during user registration: {}", e.getMessage());
            return null; // Registration failed
        }
    }

    @Override
    public String login(String email, String password) {
        try {
            logger.info("Logging in user with email: {}", email);
            // Generate a token for the logged-in user
            Optional<Registered> user = userService.getUserByUserName(email);
            if (user.isPresent()) {
                String token = tokenService.generateToken(user.get().getEmail(), user.get().getUserId());
                logger.info("User created token successfully: {}", email);
                return token;
            } else {
                logger.error("User is not present for email: {}", email);
                return null; // Login failed
            }
        } catch (Exception e) {
            logger.error("Error during user login: {}", e.getMessage());
            return null; // Login failed
        }

    }

    @Override
    public void logout(String email) {
        try {
            logger.info("Logging out user with email: {}", email);
            userService.logout(email);
            logger.info("User logged out successfully: {}", email);
        } catch (Exception e) {
            logger.error("Error during user logout: {}", e.getMessage());
        }
    }

    @Override
    public boolean isValid(String sessionToken) {
        return tokenService.validateToken(sessionToken);
    }

    public String getEmail(String sessionToken) {
        return tokenService.extractEmail(sessionToken);
    }
    public int getUserId(String sessionToken) {
        return tokenService.extractUserId(sessionToken);
    }

    public String generateGuestToken(){
        return tokenService.generateGuestToken();
    }

    public Boolean isGuestToken(String token){
        return tokenService.isGuestToken(token);
    }
}
