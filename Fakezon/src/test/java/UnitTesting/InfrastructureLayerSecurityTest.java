package UnitTesting;





import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import InfrastructureLayer.Security.TokenService;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

public class InfrastructureLayerSecurityTest {

    private TokenService tokenService;

    @BeforeEach
    void setUp() {
        tokenService = new TokenService();
    }

    @Test
    void testGenerateTokenAndValidateToken_Success() {
        String email = "test@example.com";
        int userId = 123;
        String token = tokenService.generateToken(email, userId);

        assertNotNull(token);
        assertTrue(tokenService.validateToken(token));
    }

    @Test
    void testGenerateGuestTokenAndIsGuestToken_Success() {
        String guestToken = tokenService.generateGuestToken();

        assertNotNull(guestToken);
        assertTrue(tokenService.isGuestToken(guestToken));
    }

    @Test
    void testIsGuestToken_FalseForUserToken() {
        String userToken = tokenService.generateToken("user@example.com", 42);
        assertFalse(tokenService.isGuestToken(userToken));
    }

    @Test
    void testIsGuestToken_InvalidToken() {
        assertFalse(tokenService.isGuestToken("invalid.token.value"));
    }

    @Test
    void testValidateToken_InvalidToken() {
        assertFalse(tokenService.validateToken("invalid.token.value"));
    }

    @Test
    void testExtractEmail() {
        String email = "test2@example.com";
        String token = tokenService.generateToken(email, 456);

        String extracted = tokenService.extractEmail(token);
        assertEquals(email, extracted);
    }

    @Test
    void testExtractUserId() {
        int userId = 789;
        String token = tokenService.generateToken("test3@example.com", userId);

        int extracted = tokenService.extractUserId(token);
        assertEquals(userId, extracted);
    }

    @Test
    void testExtractExpiration() {
        String token = tokenService.generateToken("test4@example.com", 101);
        Date expiration = tokenService.extractExpiration(token);

        assertNotNull(expiration);
        assertTrue(expiration.after(new Date(System.currentTimeMillis())));
    }

    @Test
    void testExtractClaim_InvalidToken() {
        // Should throw an exception for invalid token
        assertThrows(Exception.class, () -> tokenService.extractClaim("invalid.token", Claims::getSubject));
    }

    @Test
    void testExtractAllClaims_InvalidToken() {
        // Should throw an exception for invalid token
        assertThrows(Exception.class, () -> {
            // Use reflection to call private method for full coverage
            var method = TokenService.class.getDeclaredMethod("extractAllClaims", String.class);
            method.setAccessible(true);
            method.invoke(tokenService, "invalid.token");
        });
    }
    @Test
    void testExtractAllClaims_ValidToken() throws Exception {
        String token = tokenService.generateToken("test@example.com", 1);
        var method = TokenService.class.getDeclaredMethod("extractAllClaims", String.class);
        method.setAccessible(true);
        Claims claims = (Claims) method.invoke(tokenService, token);
        assertEquals("test@example.com", claims.getSubject());
    }
    @Test
    void testValidateToken_ExpiredToken() throws Exception {
        // Build a token that is already expired
        Date now = new Date();
        Date expiredDate = new Date(now.getTime() - 10000);
        String expiredToken = io.jsonwebtoken.Jwts.builder()
                .setSubject("expired@example.com")
                .setIssuedAt(new Date(now.getTime() - 20000))
                .setExpiration(expiredDate)
                .claim("userId", 1)
                .signWith(io.jsonwebtoken.security.Keys.hmacShaKeyFor(
                        "jM0Xcm6tKgvfMYdr1N5mZK3qPxYybg8vZ2H7sLDwE0RpQlFuT9aWnS4JeIcO1zA".getBytes()))
                .compact();

        assertFalse(tokenService.validateToken(expiredToken));
    }
}