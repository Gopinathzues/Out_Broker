package outbroker_backend.auth.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import outbroker_backend.auth.dto.AuthResponse;
import outbroker_backend.auth.dto.VerifyOtpRequest;
import outbroker_backend.common.enums.UserRole;
import outbroker_backend.common.enums.VerificationStatus;
import outbroker_backend.user.entity.User;
import outbroker_backend.user.entity.UserSession;
import outbroker_backend.user.repository.UserRepository;
import outbroker_backend.user.repository.UserSessionRepository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private OtpService otpService;

    @Mock
    private JwtService jwtService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserSessionRepository userSessionRepository;

    private AuthService authService;

    private User user;

    @BeforeEach
    void setUp() {
        authService = new AuthService(
                otpService,
                jwtService,
                userRepository,
                userSessionRepository
        );

        user = mock(User.class);
    }

    private void stubUserDefaults() {
        when(user.getId())
                .thenReturn(UUID.randomUUID());

        when(user.getPhoneNumber())
                .thenReturn("9876543210");

        when(user.getRole())
                .thenReturn(UserRole.TENANT);

        when(user.getVerificationStatus())
                .thenReturn(VerificationStatus.UNVERIFIED);

        when(jwtService.getRefreshExpiration())
                .thenReturn(604800000L);
    }

    @Test
    void verifyOtp_shouldRejectInvalidOtp() {
        VerifyOtpRequest request = new VerifyOtpRequest();

        request.setPhoneNumber("9876543210");
        request.setOtp("123456");

        when(otpService.validateOtp(
                "9876543210",
                "123456"
        )).thenReturn(false);

        assertThrows(
                IllegalArgumentException.class,
                () -> authService.verifyOtp(request)
        );

        verify(userRepository, never())
                .findByPhoneNumber(anyString());

        verify(jwtService, never())
                .generateToken(any(User.class));
    }

    @Test
    void verifyOtp_shouldCreateSessionForNewUser() {
        stubUserDefaults();

        VerifyOtpRequest request = new VerifyOtpRequest();

        request.setPhoneNumber("9876543210");
        request.setOtp("123456");
        request.setDeviceInfo("Chrome");

        when(otpService.validateOtp(
                "9876543210",
                "123456"
        )).thenReturn(true);

        when(userRepository.findByPhoneNumber("9876543210"))
                .thenReturn(Optional.empty());

        when(userRepository.save(any(User.class)))
                .thenReturn(user);

        when(jwtService.generateToken(user))
                .thenReturn("access-token");

        when(jwtService.generateRefreshToken(user))
                .thenReturn("refresh-token");

        AuthResponse response =
                authService.verifyOtp(request);

        assertNotNull(response);

        verify(userRepository)
                .save(any(User.class));

        ArgumentCaptor<UserSession> sessionCaptor =
                ArgumentCaptor.forClass(UserSession.class);

        verify(userSessionRepository)
                .save(sessionCaptor.capture());

        UserSession session =
                sessionCaptor.getValue();

        assertSame(user, session.getUser());
        assertEquals("Chrome", session.getDeviceInfo());
        assertFalse(session.isRevoked());
        assertNotNull(session.getRefreshTokenHash());
        assertNotNull(session.getExpiresAt());
    }

    @Test
    void verifyOtp_shouldReuseExistingUser() {
        stubUserDefaults();

        VerifyOtpRequest request = new VerifyOtpRequest();

        request.setPhoneNumber("9876543210");
        request.setOtp("123456");
        request.setDeviceInfo("Chrome");

        when(otpService.validateOtp(
                "9876543210",
                "123456"
        )).thenReturn(true);

        when(userRepository.findByPhoneNumber("9876543210"))
                .thenReturn(Optional.of(user));

        when(jwtService.generateToken(user))
                .thenReturn("access-token");

        when(jwtService.generateRefreshToken(user))
                .thenReturn("refresh-token");

        AuthResponse response =
                authService.verifyOtp(request);

        assertNotNull(response);

        verify(userRepository, never())
                .save(any(User.class));

        verify(userSessionRepository)
                .save(any(UserSession.class));
    }

    @Test
    void verifyOtp_shouldGenerateAccessAndRefreshTokens() {
        stubUserDefaults();

        VerifyOtpRequest request = new VerifyOtpRequest();

        request.setPhoneNumber("9876543210");
        request.setOtp("123456");

        when(otpService.validateOtp(
                "9876543210",
                "123456"
        )).thenReturn(true);

        when(userRepository.findByPhoneNumber("9876543210"))
                .thenReturn(Optional.of(user));

        when(jwtService.generateToken(user))
                .thenReturn("access-token");

        when(jwtService.generateRefreshToken(user))
                .thenReturn("refresh-token");

        AuthResponse response =
                authService.verifyOtp(request);

        assertNotNull(response);

        verify(jwtService)
                .generateToken(user);

        verify(jwtService)
                .generateRefreshToken(user);

        verify(userSessionRepository)
                .save(any(UserSession.class));
    }

    @Test
    void refreshToken_shouldRotateRefreshTokenAndRevokeOldSession() {
        stubUserDefaults();

        UserSession oldSession = mock(UserSession.class);

        when(oldSession.getUser())
                .thenReturn(user);

        when(oldSession.getExpiresAt())
                .thenReturn(LocalDateTime.now().plusHours(1));

        when(oldSession.getDeviceInfo())
                .thenReturn("Chrome");

        when(oldSession.getIpAddress())
                .thenReturn("127.0.0.1");

        when(userSessionRepository
                .findByRefreshTokenHashAndIsRevokedFalse(anyString()))
                .thenReturn(Optional.of(oldSession));

        when(jwtService.generateToken(user))
                .thenReturn("new-access-token");

        when(jwtService.generateRefreshToken(user))
                .thenReturn("new-refresh-token");

        AuthResponse response =
                authService.refreshToken("old-refresh-token");

        assertNotNull(response);

        verify(oldSession)
                .setRevoked(true);

        ArgumentCaptor<UserSession> sessionCaptor =
                ArgumentCaptor.forClass(UserSession.class);

        verify(userSessionRepository, times(2))
                .save(sessionCaptor.capture());

        UserSession newSession =
                sessionCaptor.getAllValues().get(1);

        assertSame(user, newSession.getUser());
        assertEquals("Chrome", newSession.getDeviceInfo());
        assertEquals("127.0.0.1", newSession.getIpAddress());
        assertFalse(newSession.isRevoked());
        assertNotNull(newSession.getRefreshTokenHash());
        assertNotNull(newSession.getExpiresAt());
    }

    @Test
    void refreshToken_shouldRejectUnknownRefreshToken() {
        when(userSessionRepository
                .findByRefreshTokenHashAndIsRevokedFalse(anyString()))
                .thenReturn(Optional.empty());

        assertThrows(
                IllegalArgumentException.class,
                () -> authService.refreshToken("unknown-token")
        );

        verify(userSessionRepository, never())
                .save(any(UserSession.class));

        verify(jwtService, never())
                .generateToken(any(User.class));
    }

    @Test
    void refreshToken_shouldRejectExpiredSessionAndRevokeIt() {
        UserSession expiredSession =
                mock(UserSession.class);

        when(expiredSession.getExpiresAt())
                .thenReturn(LocalDateTime.now().minusMinutes(1));

        when(userSessionRepository
                .findByRefreshTokenHashAndIsRevokedFalse(anyString()))
                .thenReturn(Optional.of(expiredSession));

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> authService.refreshToken("expired-token")
                );

        assertEquals(
                "Refresh token has expired",
                exception.getMessage()
        );

        verify(expiredSession)
                .setRevoked(true);

        verify(userSessionRepository)
                .save(expiredSession);

        verify(jwtService, never())
                .generateToken(any(User.class));
    }

    @Test
    void refreshToken_shouldNotCreateNewSessionWhenExpired() {
        UserSession expiredSession =
                mock(UserSession.class);

        when(expiredSession.getExpiresAt())
                .thenReturn(LocalDateTime.now().minusMinutes(1));

        when(userSessionRepository
                .findByRefreshTokenHashAndIsRevokedFalse(anyString()))
                .thenReturn(Optional.of(expiredSession));

        assertThrows(
                IllegalArgumentException.class,
                () -> authService.refreshToken("expired-token")
        );

        verify(userSessionRepository, times(1))
                .save(expiredSession);

        verify(jwtService, never())
                .generateRefreshToken(any(User.class));
    }
}