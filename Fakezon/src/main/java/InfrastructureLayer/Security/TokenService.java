package InfrastructureLayer.Security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.function.Function;

@Service
public class TokenService {
    // In production, this key should be loaded from a secure external source
    private final Key key = Keys.secretKeyFor(SignatureAlgorithm.HS256);
    private static final long JWT_TOKEN_VALIDITY = 5 * 60 * 60 * 1000; // 5 hours
    private static final String USER_ID_CLAIM = "userId";
    private static final String USER_ROLE_CLAIM = "role";
    private static final long GUEST_TOKEN_VALIDITY = 2 * 60 * 60 * 1000; // 2 hours for guests

    @Value("${jwt.secret}")
    private String secretKey; // This should be set in application.properties
    
    // Generate token for user with userId
    public String generateToken(String email, int userId) {
        return Jwts.builder()
                .setSubject(email)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + JWT_TOKEN_VALIDITY))
                .claim(USER_ID_CLAIM, userId)
                .signWith(key)
                .compact();
        
    }

    /**
     * Generate a token for guest users
     * @return JWT token with guest role
     */
    public String generateGuestToken() {
        String guestId = "guest-" + java.util.UUID.randomUUID().toString();
        
        return Jwts.builder()
                .setSubject(guestId)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + GUEST_TOKEN_VALIDITY))
                .claim(USER_ROLE_CLAIM, "GUEST")
                .signWith(key)
                .compact();
    }

    /**
     * Check if token belongs to a guest user
     * @param token JWT token to check
     * @return true if token is for a guest user
     */
    public boolean isGuestToken(String token) {
        try {
            String role = extractClaim(token, claims -> claims.get(USER_ROLE_CLAIM, String.class));
            return "GUEST".equals(role);
        } catch (Exception e) {
            return false;
        }
    }

    // Validate token
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token);
            return !isTokenExpired(token);
        } catch (Exception e) {
            return false;
        }
    }

    // Extract email from token
    public String extractEmail(String token) {
        return extractClaim(token, Claims::getSubject);
    }
    
    // Extract userId from token
    public int extractUserId(String token) {
        return extractClaim(token, claims -> claims.get(USER_ID_CLAIM, Integer.class));
    }

    // Extract expiration date from token
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    // Extract specific claim from token
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    // Extract all claims from token
    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    // Check if token is expired
    private boolean isTokenExpired(String token) {
        final Date expiration = extractExpiration(token);
        return expiration.before(new Date());
    }

}