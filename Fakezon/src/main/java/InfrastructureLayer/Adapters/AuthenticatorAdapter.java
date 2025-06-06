package InfrastructureLayer.Adapters;

import java.time.LocalDate;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import ApplicationLayer.DTO.UserDTO;
import ApplicationLayer.Interfaces.IUserService;
import DomainLayer.Interfaces.IAuthenticator;
import DomainLayer.Model.Registered;
import InfrastructureLayer.Security.TokenService;

import ApplicationLayer.Response;
import ApplicationLayer.Enums.ErrorType;

@Component
public class AuthenticatorAdapter implements IAuthenticator {
    private TokenService tokenService;
    private IUserService userService;
    private static final Logger logger = LoggerFactory.getLogger(AuthenticatorAdapter.class);

    public AuthenticatorAdapter(IUserService userService) {
        this.tokenService = new TokenService();
        this.userService = userService;
    }

    @Override
    public Response<String> register(String email, String password, LocalDate dateOfBirth, String country) {
        try {
            logger.info("Registering user with email: {}", email);
            UserDTO userDTO = userService.registerUser(email, password, dateOfBirth, country);
            if (userDTO != null) {
                logger.info("User registered successfully: {}", userDTO.getUserEmail());
                // Generate a token for the registered user with userId
                String token = tokenService.generateToken(userDTO.getUserEmail(), userDTO.getUserId());
                //return token;
                return new Response<>(token, "Registration successful", true, null, token);
            } else {
                logger.error("User registration failed for email: {}", email);
                //return null; // Registration failed
                return new Response<>(null, "Registration failed", false, ErrorType.INTERNAL_ERROR, null);
            }
        } catch (Exception e) {
            logger.error("Error during user registration: {}", e.getMessage());
            //return null; // Registration failed
            return new Response<>(null, "Registration failed: " + e.getMessage(), false, ErrorType.INVALID_INPUT, null);

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

    @Override
    public String getEmail(String sessionToken) {
        return tokenService.extractEmail(sessionToken);
    }
    @Override
    public int getUserId(String sessionToken) {
        return tokenService.extractUserId(sessionToken);
    }
    @Override
    public String generateGuestToken(){
        return tokenService.generateGuestToken();
    }
    @Override
    public Boolean isGuestToken(String token){
        return tokenService.isGuestToken(token);
    }
}
