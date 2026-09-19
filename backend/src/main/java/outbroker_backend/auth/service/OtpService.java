package outbroker_backend.auth.service;

import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class OtpService {

    private static final int OTP_LENGTH = 6;

    // OTP verification security
    private static final int MAX_ATTEMPTS = 5;
    private static final Duration OTP_EXPIRY = Duration.ofMinutes(5);

    // OTP request rate limiting
    private static final Duration RESEND_COOLDOWN = Duration.ofSeconds(60);
    private static final Duration REQUEST_WINDOW = Duration.ofHours(1);
    private static final int MAX_REQUESTS_PER_WINDOW = 5;

    private final SecureRandom secureRandom = new SecureRandom();

    private final Map<String, OtpEntry> otpStorage =
            new ConcurrentHashMap<>();

    private final Map<String, Deque<Instant>> requestHistory =
            new ConcurrentHashMap<>();

    /**
     * Generates and sends an OTP.
     *
     * Rate limits:
     * - Maximum one OTP every 60 seconds per phone number.
     * - Maximum five OTP requests within one hour per phone number.
     */
    public synchronized void generateAndSendOtp(String phoneNumber) {

        validatePhoneNumber(phoneNumber);

        Instant now = Instant.now();

        Deque<Instant> requests = requestHistory.computeIfAbsent(
                phoneNumber,
                key -> new ArrayDeque<>()
        );

        // Remove requests outside the rolling one-hour window.
        while (!requests.isEmpty()
                && requests.peekFirst().plus(REQUEST_WINDOW).isBefore(now)) {

            requests.pollFirst();
        }

        // 60-second resend protection.
        if (!requests.isEmpty()) {

            Instant lastRequest = requests.peekLast();

            if (lastRequest.plus(RESEND_COOLDOWN).isAfter(now)) {

                long remainingSeconds = Duration.between(
                        now,
                        lastRequest.plus(RESEND_COOLDOWN)
                ).getSeconds();

                throw new OtpRateLimitException(
                        "Please wait "
                                + Math.max(1, remainingSeconds)
                                + " seconds before requesting another OTP."
                );
            }
        }

        // Hourly request limit.
        if (requests.size() >= MAX_REQUESTS_PER_WINDOW) {

            long remainingMinutes = Math.max(
                    1,
                    Duration.between(
                            now,
                            requests.peekFirst().plus(REQUEST_WINDOW)
                    ).toMinutes()
            );

            throw new OtpRateLimitException(
                    "OTP request limit exceeded. Please try again in "
                            + remainingMinutes
                            + " minutes."
            );
        }

        String otp = generateOtp();

        otpStorage.put(
                phoneNumber,
                new OtpEntry(
                        otp,
                        now.plus(OTP_EXPIRY),
                        0
                )
        );

        requests.addLast(now);

        // Development-only mock SMS service.
        // Replace this with the real SMS provider later.
        System.out.println(
                "[MOCK SMS SERVICE] Sent OTP "
                        + otp
                        + " to "
                        + phoneNumber
        );
    }

    /**
     * Validates an OTP.
     *
     * Security rules:
     * - OTP expires after five minutes.
     * - Maximum five failed attempts.
     * - Successful OTP is immediately invalidated.
     */
    public boolean validateOtp(String phoneNumber, String otp) {

        validatePhoneNumber(phoneNumber);

        if (otp == null || otp.length() != OTP_LENGTH) {
            return false;
        }

        OtpEntry entry = otpStorage.get(phoneNumber);

        if (entry == null) {
            return false;
        }

        Instant now = Instant.now();

        // Expired OTP.
        if (now.isAfter(entry.expiresAt())) {

            otpStorage.remove(phoneNumber);

            return false;
        }

        // Maximum attempts reached.
        if (entry.attempts() >= MAX_ATTEMPTS) {

            otpStorage.remove(phoneNumber);

            return false;
        }

        // Invalid OTP.
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

        // OTP is single-use.
        otpStorage.remove(phoneNumber);

        return true;
    }

    private String generateOtp() {

        int upperBound = (int) Math.pow(10, OTP_LENGTH);

        int otpNumber = secureRandom.nextInt(upperBound);

        return String.format(
                "%0" + OTP_LENGTH + "d",
                otpNumber
        );
    }

    private void validatePhoneNumber(String phoneNumber) {

        if (phoneNumber == null || phoneNumber.isBlank()) {

            throw new IllegalArgumentException(
                    "Phone number is required"
            );
        }
    }

    private record OtpEntry(
            String otp,
            Instant expiresAt,
            int attempts
    ) {
    }

    /**
     * Keeps OTP request throttling failures separate from
     * normal validation/business errors.
     */
    public static class OtpRateLimitException
            extends RuntimeException {

        public OtpRateLimitException(String message) {
            super(message);
        }
    }
}