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

    public String register(String email, String password, LocalDate dateOfBirth) {
        try {
            logger.info("Registering user with email: {}", email);
            UserDTO userDTO = userService.registerUser(email, password, dateOfBirth);
            if (userDTO != null) {
                logger.info("User registered successfully: {}", userDTO.getUserEmail());
                // Generate a token for the registered user
                String token = tokenService.generateToken(userDTO.getUserEmail());
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

    public String login(String email, String password) {
        try {
            logger.info("Logging in user with email: {}", email);
            userService.login(email, password);
            // Generate a token for the logged-in user
            Optional<Registered> user = userService.getUserByUserName(email);
            if (user.isPresent()) {
                String token = tokenService.generateToken(user.get().getEmail());
                logger.info("User logged in successfully: {}", email);
                return token;
            } else {
                logger.error("User login failed for email: {}", email);
                return null; // Login failed
            }
        } catch (Exception e) {
            logger.error("Error during user login: {}", e.getMessage());
            return null; // Login failed
        }

    }

    public void logout(String email) {
        try {
            logger.info("Logging out user with email: {}", email);
            userService.logout(email);
            logger.info("User logged out successfully: {}", email);
        } catch (Exception e) {
            logger.error("Error during user logout: {}", e.getMessage());
        }
    }

    public boolean isValid(String sessionToken) {
        return tokenService.validateToken(sessionToken);
    }
}
