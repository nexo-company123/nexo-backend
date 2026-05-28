package pl.edu.uj.tp.nexo.security.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import pl.edu.uj.tp.nexo.organization.entity.Organization;
import pl.edu.uj.tp.nexo.user.entity.Role;
import pl.edu.uj.tp.nexo.user.entity.User;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JwtServiceTest {

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "secretKey",
                "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970");
        ReflectionTestUtils.setField(jwtService, "jwtExpiration", 60_000L);
    }

    private User makeUser() {
        Organization organization = new Organization();
        organization.setId(42L);
        organization.setName("Nexo Org");

        return User.builder()
                .id(1L)
                .email("jan@example.com")
                .firstName("Jan")
                .lastName("Kowalski")
                .password("password")
                .role(Role.ADMIN)
                .organization(organization)
                .build();
    }

    @Test
    void generateToken_returnsNonEmptyTokenWithUserEmailAsSubject() {
        User user = makeUser();

        String token = jwtService.generateToken(user);

        assertNotNull(token);
        assertFalse(token.isBlank());
        assertEquals("jan@example.com", jwtService.extractUsername(token));
    }

    @Test
    void isTokenValid_freshTokenForSameUser_returnsTrue() {
        User user = makeUser();
        String token = jwtService.generateToken(user);

        assertTrue(jwtService.isTokenValid(token, user));
    }

    @Test
    void isTokenValid_tokenWithExpiredLifetime_throwsExpiredJwtException() throws Exception {
        ReflectionTestUtils.setField(jwtService, "jwtExpiration", 1L);
        User user = makeUser();
        String token = jwtService.generateToken(user);

        Thread.sleep(50);

        assertThrows(ExpiredJwtException.class, () -> jwtService.isTokenValid(token, user));
    }

    @Test
    void generateToken_expirationIsInTheFuture() {
        User user = makeUser();
        String token = jwtService.generateToken(user);

        Date expiration = jwtService.extractClaim(token, Claims::getExpiration);

        assertTrue(expiration.after(new Date()));
    }

    @Test
    void extractClaim_extractsCustomRoleAndOrganizationIdClaims() {
        User user = makeUser();
        String token = jwtService.generateToken(user);

        String role = jwtService.extractClaim(token, claims -> claims.get("role", String.class));
        Long organizationId = jwtService.extractClaim(token, claims -> claims.get("organizationId", Long.class));

        assertEquals("ADMIN", role);
        assertEquals(42L, organizationId);
    }
}
