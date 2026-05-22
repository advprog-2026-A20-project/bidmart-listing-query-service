package id.ac.ui.cs.advprog.listingquery.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtServiceTest {

    private static final String SECRET = "test-secret-for-listing-query-12345";

    private final JwtService jwtService = new JwtService(SECRET);

    @Test
    void validTokenShouldExposeExpectedClaims() {
        UUID userId = UUID.randomUUID();
        String token = Jwts.builder()
            .setSubject(userId.toString())
            .claim("email", "seller@example.com")
            .claim("role", "SELLER")
            .signWith(Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8)))
            .compact();

        assertThat(jwtService.isValid(token)).isTrue();
        assertThat(jwtService.extractUserId(token)).isEqualTo(userId.toString());
        assertThat(jwtService.extractEmail(token)).isEqualTo("seller@example.com");
        assertThat(jwtService.extractRole(token)).isEqualTo("SELLER");
    }

    @Test
    void invalidTokenShouldBeRejected() {
        assertThat(jwtService.isValid("not-a-jwt")).isFalse();
    }

    @Test
    void shortSecretShouldBeRejected() {
        assertThatThrownBy(() -> new JwtService("too-short"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("JWT secret must be at least 32 characters");
    }
}
