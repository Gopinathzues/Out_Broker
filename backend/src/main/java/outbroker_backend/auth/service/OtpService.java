package outbroker_backend.auth.service;

import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class OtpService {

    // In-memory storage for development/testing (phoneNumber -> otp)
    private final Map<String, String> otpStorage = new ConcurrentHashMap<>();

    public void generateAndSendOtp(String phoneNumber) {
        String mockOtp = "123456";
        otpStorage.put(phoneNumber, mockOtp);
        System.out.println("[MOCK SMS SERVICE] Sent OTP " + mockOtp + " to " + phoneNumber);
    }

    public boolean validateOtp(String phoneNumber, String otp) {
        String storedOtp = otpStorage.get(phoneNumber);
        if (storedOtp != null && storedOtp.equals(otp)) {
            otpStorage.remove(phoneNumber);
            return true;
        }
        return false;
    }
}