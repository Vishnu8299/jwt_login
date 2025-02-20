package com.example.jwtdemo.service;

import com.example.jwtdemo.dto.PasswordResetRequest;
import com.example.jwtdemo.exception.ApiException;
import com.example.jwtdemo.model.*;
import com.example.jwtdemo.repository.UserRepository;
import com.example.jwtdemo.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Service class for handling authentication-related operations.
 */
@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private EmailService emailService;

    @Autowired
    private TokenService tokenService;

    @Autowired
    private RateLimitService rateLimitService;

    @Value("${app.reset-token.expiration:3600}") // 1 hour default
    private long resetTokenExpiration;

    // Predefined admin credentials
    @Value("${admin.email:admin@msme.gov.in}")
    private String ADMIN_EMAIL;

    @Value("${admin.password:Admin@MSME2025}")
    private String ADMIN_PASSWORD;

    /**
     * Handles user login with email and role.
     *
     * @param loginRequest Login request containing email, password, and role.
     * @return Mono of LoginResponse containing JWT token, role, user ID, and name.
     */
    public Mono<LoginResponse> login(LoginRequest loginRequest) {
        // Check for predefined admin credentials
        if (loginRequest.getEmail().equals(ADMIN_EMAIL) && 
            loginRequest.getPassword().equals(ADMIN_PASSWORD)) {
            
            if (loginRequest.getRole() != UserRole.ADMIN) {
                return Mono.error(new ApiException(
                    HttpStatus.UNAUTHORIZED,
                    "Invalid role for admin login",
                    "INVALID_ROLE"
                ));
            }
            
            // Create a login response for the admin
            LoginResponse adminResponse = new LoginResponse(
                jwtUtil.generateToken(ADMIN_EMAIL), 
                UserRole.ADMIN, 
                "admin", 
                "MSME Admin"
            );
            return Mono.just(adminResponse);
        }

        // Regular user login
        return userRepository.findByEmailAndRole(loginRequest.getEmail(), loginRequest.getRole())
                .filter(user -> passwordEncoder.matches(loginRequest.getPassword(), user.getPassword()))
                .map(this::createLoginResponse)
                .switchIfEmpty(Mono.error(new ApiException(
                    HttpStatus.UNAUTHORIZED,
                    "Invalid credentials or role",
                    "INVALID_CREDENTIALS"
                )));
    }

    /**
     * Registers a new developer user.
     *
     * @param user User to be registered.
     * @return Mono of LoginResponse containing JWT token, role, user ID, and name.
     */
    public Mono<LoginResponse> registerDeveloper(User user) {
        user.setRole(UserRole.DEVELOPER);
        return registerUser(user).map(this::createLoginResponse);
    }

    /**
     * Registers a new buyer user.
     *
     * @param user User to be registered.
     * @return Mono of LoginResponse containing JWT token, role, user ID, and name.
     */
    public Mono<LoginResponse> registerBuyer(User user) {
        if (user.getOrganization() == null || user.getOrganization().trim().isEmpty()) {
            return Mono.error(new ApiException(
                HttpStatus.BAD_REQUEST,
                "Organization is required for buyers",
                "ORGANIZATION_REQUIRED"
            ));
        }
        user.setRole(UserRole.BUYER);
        return registerUser(user).map(this::createLoginResponse);
    }

    private LoginResponse createLoginResponse(User user) {
        String token = jwtUtil.generateToken(user.getEmail());
        return new LoginResponse(token, user.getRole(), user.getId(), user.getName());
    }

    /**
     * Registers a new user with the given role.
     *
     * @param user User to be registered.
     * @return Mono of registered User.
     */
    private Mono<User> registerUser(User user) {
        // Prevent registration with admin email
        if (user.getEmail().equals(ADMIN_EMAIL)) {
            return Mono.error(new ApiException(
                HttpStatus.BAD_REQUEST,
                "This email is reserved",
                "EMAIL_RESERVED"
            ));
        }

        return userRepository.findByEmail(user.getEmail())
                .hasElement()
                .flatMap(exists -> {
                    if (exists) {
                        return Mono.error(new ApiException(
                            HttpStatus.BAD_REQUEST,
                            "Email already exists",
                            "EMAIL_EXISTS"
                        ));
                    }
                    user.setPassword(passwordEncoder.encode(user.getPassword()));
                    user.setCreatedAt(LocalDateTime.now().toString());
                    user.setActive(true);
                    return userRepository.save(user);
                });
    }

    public Mono<Void> initiatePasswordReset(String email) {
        return rateLimitService.checkRateLimit(email, "password_reset")
                .flatMap(result -> userRepository.findByEmail(email))
                .switchIfEmpty(Mono.error(new ApiException(
                        HttpStatus.NOT_FOUND,
                        "If an account exists with this email, you will receive password reset instructions",
                        "USER_NOT_FOUND"
                )))
                .flatMap(user -> {
                    String resetToken = UUID.randomUUID().toString();
                    return tokenService.saveResetToken(user.getId(), resetToken, Duration.ofSeconds(resetTokenExpiration))
                            .then(sendResetEmail(user.getEmail(), resetToken));
                });
    }

    public Mono<Void> resetPassword(PasswordResetRequest request) {
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            return Mono.error(new ApiException(
                HttpStatus.BAD_REQUEST,
                "Passwords do not match",
                "PASSWORD_MISMATCH"
            ));
        }

        return tokenService.getUserIdFromResetToken(request.getToken())
                .switchIfEmpty(Mono.error(new ApiException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid or expired reset token",
                    "INVALID_RESET_TOKEN"
                )))
                .flatMap(userId -> 
                    rateLimitService.checkRateLimit(userId, "password_change")
                        .then(userRepository.findById(userId))
                )
                .flatMap(user -> {
                    // Check if new password is different from the current one
                    if (passwordEncoder.matches(request.getNewPassword(), user.getPassword())) {
                        return Mono.error(new ApiException(
                            HttpStatus.BAD_REQUEST,
                            "New password must be different from the current password",
                            "SAME_PASSWORD"
                        ));
                    }

                    user.setPassword(passwordEncoder.encode(request.getNewPassword()));
                    return userRepository.save(user)
                            .then(tokenService.removeResetToken(request.getToken()))
                            .then(rateLimitService.resetRateLimit(user.getId(), "password_change"))
                            .then(sendPasswordChangeNotification(user.getEmail()));
                });
    }

    public Mono<Boolean> verifyResetToken(String token) {
        return tokenService.getUserIdFromResetToken(token)
                .map(userId -> true)
                .defaultIfEmpty(false);
    }

    private Mono<Void> sendResetEmail(String email, String resetToken) {
        String resetLink = "http://your-frontend-url/reset-password?token=" + resetToken;
        String subject = "Password Reset Request";
        String content = String.format("""
            Hello,
            
            You have requested to reset your password. Please click the link below to reset your password:
            
            %s
            
            This link will expire in 1 hour.
            
            If you did not request this, please ignore this email and ensure your account is secure.
            
            Best regards,
            Your Application Team
            """, resetLink);

        return Mono.fromRunnable(() -> emailService.sendEmail(email, subject, content));
    }

    private Mono<Void> sendPasswordChangeNotification(String email) {
        String subject = "Password Changed Successfully";
        String content = """
            Hello,
            
            Your password has been successfully changed.
            
            If you did not make this change, please contact our support team immediately.
            
            Best regards,
            Your Application Team
            """;

        return Mono.fromRunnable(() -> emailService.sendEmail(email, subject, content));
    }

    /**
     * Refreshes the JWT token for a user.
     *
     * @param token The current JWT token to refresh
     * @return Mono of LoginResponse containing the new JWT token and user details
     */
    public Mono<LoginResponse> refreshToken(String token) {
        if (token == null || token.trim().isEmpty()) {
            return Mono.error(new ApiException(
                HttpStatus.BAD_REQUEST,
                "Token cannot be empty",
                "INVALID_TOKEN"
            ));
        }

        try {
            String email = jwtUtil.extractUsername(token);
            if (email != null && jwtUtil.isTokenValid(token, email)) {
                return userRepository.findByEmail(email)
                    .map(user -> {
                        String newToken = jwtUtil.generateToken(email);
                        return new LoginResponse(newToken, user.getRole(), user.getId(), user.getName());
                    })
                    .switchIfEmpty(Mono.error(new ApiException(
                        HttpStatus.NOT_FOUND,
                        "User not found",
                        "USER_NOT_FOUND"
                    )));
            }
            return Mono.error(new ApiException(
                HttpStatus.UNAUTHORIZED,
                "Invalid or expired token",
                "INVALID_TOKEN"
            ));
        } catch (Exception e) {
            return Mono.error(new ApiException(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Error refreshing token: " + e.getMessage(),
                "TOKEN_REFRESH_ERROR"
            ));
        }
    }

    /**
     * Handles user logout by invalidating the current token.
     *
     * @param token The JWT token to invalidate
     * @return Mono<Void> indicating successful logout
     */
    public Mono<Void> logout(String token) {
        if (token == null || token.trim().isEmpty()) {
            return Mono.error(new ApiException(
                HttpStatus.BAD_REQUEST,
                "Token cannot be empty",
                "INVALID_TOKEN"
            ));
        }

        try {
            String email = jwtUtil.extractUsername(token);
            if (email == null || !jwtUtil.isTokenValid(token, email)) {
                return Mono.error(new ApiException(
                    HttpStatus.UNAUTHORIZED,
                    "Invalid or expired token",
                    "INVALID_TOKEN"
                ));
            }
            
            // Invalidate the token by adding it to the TokenService's blacklist
            return tokenService.blacklistToken(token)
                .then(Mono.empty());
        } catch (Exception e) {
            return Mono.error(new ApiException(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Error during logout: " + e.getMessage(),
                "LOGOUT_ERROR"
            ));
        }
    }
}
