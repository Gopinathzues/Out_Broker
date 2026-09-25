package outbroker_backend.auth.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import outbroker_backend.user.entity.User;
import outbroker_backend.common.enums.UserRole;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class JwtServiceTest {

    private JwtService jwtService;
    private User user;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();

        byte[] secretBytes =
                "01234567890123456789012345678901"
                        .getBytes(StandardCharsets.UTF_8);

        String base64Secret =
                Base64.getEncoder().encodeToString(secretBytes);

        ReflectionTestUtils.setField(
                jwtService,
                "secretKey",
                base64Secret
        );

        ReflectionTestUtils.setField(
                jwtService,
                "jwtExpiration",
                60_000L
        );

        ReflectionTestUtils.setField(
                jwtService,
                "refreshExpiration",
                120_000L
        );

        user = mock(User.class);

        when(user.getId())
                .thenReturn(UUID.randomUUID());

        when(user.getPhoneNumber())
                .thenReturn("9876543210");

        when(user.getRole())
                .thenReturn(UserRole.TENANT);
    }

    @Test
    void generateToken_shouldCreateValidToken() {
        String token =
                jwtService.generateToken(user);

        assertNotNull(token);
        assertFalse(token.isBlank());

        assertEquals(
                "9876543210",
                jwtService.extractPhoneNumber(token)
        );
    }

    @Test
    void isTokenValid_shouldReturnTrue_forValidTokenAndMatchingUser() {
        String token =
                jwtService.generateToken(user);

        assertTrue(
                jwtService.isTokenValid(token, user)
        );
    }

    @Test
    void isTokenValid_shouldReturnFalse_forDifferentUser() {
        String token =
                jwtService.generateToken(user);

        User anotherUser = mock(User.class);

        when(anotherUser.getPhoneNumber())
                .thenReturn("9876543211");

        assertFalse(
                jwtService.isTokenValid(token, anotherUser)
        );
    }

    @Test
    void generateRefreshToken_shouldCreateValidRefreshToken() {
        String token =
                jwtService.generateRefreshToken(user);

        assertNotNull(token);
        assertFalse(token.isBlank());

        assertEquals(
                "9876543210",
                jwtService.extractPhoneNumber(token)
        );
    }

    @Test
    void getRefreshExpiration_shouldReturnConfiguredValue() {
        assertEquals(
                120_000L,
                jwtService.getRefreshExpiration()
        );
    }

    @Test
    void extractPhoneNumber_shouldFail_forTamperedToken() {
        String token =
                jwtService.generateToken(user);

        String tamperedToken =
                token.substring(0, token.length() - 2) + "xx";

        assertThrows(
                Exception.class,
                () -> jwtService.extractPhoneNumber(tamperedToken)
        );
    }
}