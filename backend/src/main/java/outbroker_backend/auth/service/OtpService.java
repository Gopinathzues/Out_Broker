package outbroker_backend.auth.service;

import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class OtpService {

    private static final int OTP_LENGTH = 6;
    private static final int MAX_ATTEMPTS = 5;
    private static final Duration OTP_EXPIRY = Duration.ofMinutes(5);

    private final SecureRandom secureRandom = new SecureRandom();

    private final Map<String, OtpEntry> otpStorage =
            new ConcurrentHashMap<>();

    public void generateAndSendOtp(String phoneNumber) {

        String otp = generateOtp();

        otpStorage.put(
                phoneNumber,
                new OtpEntry(
                        otp,
                        Instant.now().plus(OTP_EXPIRY),
                        0
                )
        );

        // Development-only mock SMS service
        System.out.println(
                "[MOCK SMS SERVICE] Sent OTP "
                        + otp
                        + " to "
                        + phoneNumber
        );
    }

    public boolean validateOtp(String phoneNumber, String otp) {

        OtpEntry entry = otpStorage.get(phoneNumber);

        if (entry == null) {
            return false;
        }

        if (Instant.now().isAfter(entry.expiresAt())) {
            otpStorage.remove(phoneNumber);
            return false;
        }

        if (entry.attempts() >= MAX_ATTEMPTS) {
            otpStorage.remove(phoneNumber);
            return false;
        }

        if (!entry.otp().equals(otp)) {

            int updatedAttempts = entry.attempts() + 1;

            if (updatedAttempts >= MAX_ATTEMPTS) {
                otpStorage.remove(phoneNumber);
            } else {
                otpStorage.put(
                        phoneNumber,
                        new OtpEntry(
                                entry.otp(),
                                entry.expiresAt(),
                                updatedAttempts
                        )
                );
            }

            return false;
        }

        // OTP is single-use
        otpStorage.remove(phoneNumber);

        return true;
    }

    private String generateOtp() {

        int upperBound = (int) Math.pow(10, OTP_LENGTH);

        int otpNumber = secureRandom.nextInt(upperBound);

        return String.format("%0" + OTP_LENGTH + "d", otpNumber);
    }

    private record OtpEntry(
            String otp,
            Instant expiresAt,
            int attempts
    ) {
    }
}