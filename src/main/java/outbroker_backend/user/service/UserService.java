package outbroker_backend.user.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import outbroker_backend.user.dto.UpdateProfileRequest;
import outbroker_backend.user.entity.User;
import outbroker_backend.user.repository.UserRepository;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional
    public User updateProfile(User user, UpdateProfileRequest request) {
        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());
        user.setRole(request.getRole());
        return userRepository.save(user);
    }
}