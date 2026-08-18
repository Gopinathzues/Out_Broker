package outbroker_backend.user.controller;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import outbroker_backend.common.dto.ApiResponse;
import outbroker_backend.user.dto.UpdateProfileRequest;
import outbroker_backend.user.entity.User;
import outbroker_backend.user.service.UserService;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<User>> getCurrentUser(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(ApiResponse.success("User profile retrieved", user));
    }

    @PutMapping("/me")
    public ResponseEntity<ApiResponse<User>> updateProfile(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody UpdateProfileRequest request
    ) {
        User updatedUser = userService.updateProfile(user, request);
        return ResponseEntity.ok(ApiResponse.success("Profile updated successfully", updatedUser));
    }
}