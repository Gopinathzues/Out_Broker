package outbroker_backend.auth.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;

class OtpServiceTest {

    private OtpService otpService;

    @BeforeEach
    void setUp() {
        otpService = new OtpService();
    }

    @Test
    void validateOtp_shouldReturnFalse_whenNoOtpWasGenerated() {
        boolean result =
                otpService.validateOtp("9876543210", "123456");

        assertFalse(result);
    }

    @Test
    void validateOtp_shouldReturnFalse_whenOtpIsInvalid() {
        String phoneNumber = "9876543210";

        String output = captureOutput(() ->
                otpService.generateAndSendOtp(phoneNumber)
        );

        assertNotNull(output);

        boolean result =
                otpService.validateOtp(phoneNumber, "000000");

        assertFalse(result);
    }

    @Test
    void validateOtp_shouldReturnTrue_whenOtpIsCorrect() {
        String phoneNumber = "9876543211";

        String output = captureOutput(() ->
                otpService.generateAndSendOtp(phoneNumber)
        );

        String otp = extractOtp(output);

        assertNotNull(otp);
        assertEquals(6, otp.length());

        boolean result =
                otpService.validateOtp(phoneNumber, otp);

        assertTrue(result);
    }

    @Test
    void validateOtp_shouldRejectOtpAfterSuccessfulVerification() {
        String phoneNumber = "9876543212";

        String output = captureOutput(() ->
                otpService.generateAndSendOtp(phoneNumber)
        );

        String otp = extractOtp(output);

        assertNotNull(otp);

        assertTrue(
                otpService.validateOtp(phoneNumber, otp)
        );

        assertFalse(
                otpService.validateOtp(phoneNumber, otp)
        );
    }

    @Test
    void validateOtp_shouldRejectOtpAfterFiveFailedAttempts() {
        String phoneNumber = "9876543213";

        captureOutput(() ->
                otpService.generateAndSendOtp(phoneNumber)
        );

        for (int i = 0; i < 5; i++) {
            assertFalse(
                    otpService.validateOtp(phoneNumber, "000000")
            );
        }

        assertFalse(
                otpService.validateOtp(phoneNumber, "000000")
        );
    }

    @Test
    void generateAndSendOtp_shouldRejectImmediateResend() {
        String phoneNumber = "9876543214";

        captureOutput(() ->
                otpService.generateAndSendOtp(phoneNumber)
        );

        assertThrows(
                OtpService.OtpRateLimitException.class,
                () -> otpService.generateAndSendOtp(phoneNumber)
        );
    }

    @Test
    void validateOtp_shouldRejectNullOtp() {
        assertFalse(
                otpService.validateOtp("9876543215", null)
        );
    }

    @Test
    void validateOtp_shouldRejectOtpWithWrongLength() {
        assertFalse(
                otpService.validateOtp("9876543216", "12345")
        );

        assertFalse(
                otpService.validateOtp("9876543216", "1234567")
        );
    }

    @Test
    void generateAndSendOtp_shouldRejectBlankPhoneNumber() {
        assertThrows(
                IllegalArgumentException.class,
                () -> otpService.generateAndSendOtp("")
        );
    }

    private String captureOutput(Runnable action) {
        PrintStream originalOut = System.out;

        ByteArrayOutputStream output =
                new ByteArrayOutputStream();

        System.setOut(new PrintStream(output));

        try {
            action.run();
            return output.toString();
        } finally {
            System.setOut(originalOut);
        }
    }

    private String extractOtp(String output) {
        Pattern pattern =
                Pattern.compile("Sent OTP (\\d{6})");

        Matcher matcher = pattern.matcher(output);

        if (matcher.find()) {
            return matcher.group(1);
        }

        return null;
    }
}