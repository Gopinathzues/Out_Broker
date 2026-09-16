package outbroker_backend.auth.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import outbroker_backend.auth.dto.AuthResponse;
import outbroker_backend.auth.dto.SendOtpRequest;
import outbroker_backend.auth.dto.VerifyOtpRequest;
import outbroker_backend.user.entity.User;
import outbroker_backend.user.entity.UserSession;
import outbroker_backend.user.repository.UserRepository;
import outbroker_backend.user.repository.UserSessionRepository;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;

@Service
public class AuthService {

    private final OtpService otpService;
    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final UserSessionRepository userSessionRepository;

    public AuthService(
            OtpService otpService,
            JwtService jwtService,
            UserRepository userRepository,
            UserSessionRepository userSessionRepository
    ) {
        this.otpService = otpService;
        this.jwtService = jwtService;
        this.userRepository = userRepository;
        this.userSessionRepository = userSessionRepository;
    }

    public void sendOtp(SendOtpRequest request) {
        otpService.generateAndSendOtp(request.getPhoneNumber());
    }

    @Transactional
    public AuthResponse verifyOtp(VerifyOtpRequest request) {
        boolean isValid = otpService.validateOtp(request.getPhoneNumber(), request.getOtp());
        if (!isValid) {
            throw new IllegalArgumentException("Invalid or expired OTP");
        }

        User user = userRepository.findByPhoneNumber(request.getPhoneNumber())
                .orElseGet(() -> {
                    User newUser = new User(request.getPhoneNumber());
                    return userRepository.save(newUser);
                });

        String accessToken = jwtService.generateToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        UserSession session = new UserSession(
                user,
                hashToken(refreshToken),
                request.getDeviceInfo(),
                null,
                LocalDateTime.now().plusDays(7)
        );
        userSessionRepository.save(session);

        return new AuthResponse(
                accessToken,
                refreshToken,
                user.getId(),
                user.getPhoneNumber(),
                user.getRole(),
                user.getVerificationStatus()
        );
    }

    @Transactional
    public AuthResponse refreshToken(String refreshToken) {
        String hashedToken = hashToken(refreshToken);

        UserSession session = userSessionRepository.findByRefreshTokenHashAndIsRevokedFalse(hashedToken)
                .orElseThrow(() -> new IllegalArgumentException("Invalid or revoked refresh token"));

        if (session.getExpiresAt().isBefore(LocalDateTime.now())) {
            session.setRevoked(true);
            userSessionRepository.save(session);
            throw new IllegalArgumentException("Refresh token has expired");
        }

        User user = session.getUser();
        String newAccessToken = jwtService.generateToken(user);
        String newRefreshToken = jwtService.generateRefreshToken(user);

        session.setRevoked(true);
        userSessionRepository.save(session);

        UserSession newSession = new UserSession(
                user,
                hashToken(newRefreshToken),
                session.getDeviceInfo(),
                session.getIpAddress(),
                LocalDateTime.now().plusNanos(jwtService.getRefreshExpiration() * 1_000_000)
        );
        userSessionRepository.save(newSession);

        return new AuthResponse(
                newAccessToken,
                newRefreshToken,
                user.getId(),
                user.getPhoneNumber(),
                user.getRole(),
                user.getVerificationStatus()
        );
    }

    private String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] encodedhash = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder(2 * encodedhash.length);
            for (byte b : encodedhash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not found", e);
        }
    }
}