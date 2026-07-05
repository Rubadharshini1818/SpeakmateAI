package com.speakmate.backend.controller;

import com.speakmate.backend.model.dto.*;
import com.speakmate.backend.model.entity.User;
import com.speakmate.backend.model.entity.UserStats;
import com.speakmate.backend.repository.UserRepository;
import com.speakmate.backend.repository.UserStatsRepository;
import com.speakmate.backend.security.JwtTokenProvider;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserRepository userRepository;
    private final UserStatsRepository userStatsRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;

    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@Valid @RequestBody SignupRequest signUpRequest) {
        if (userRepository.existsByUsername(signUpRequest.getUsername())) {
            return ResponseEntity
                    .badRequest()
                    .body(Map.of("message", "Error: Username is already taken!"));
        }

        if (userRepository.existsByEmail(signUpRequest.getEmail())) {
            return ResponseEntity
                    .badRequest()
                    .body(Map.of("message", "Error: Email Address already in use!"));
        }

        // Creating user's account
        User user = User.builder()
                .username(signUpRequest.getUsername())
                .email(signUpRequest.getEmail())
                .passwordHash(passwordEncoder.encode(signUpRequest.getPassword()))
                .learningLevel("B1") // default learning level
                .build();

        User savedUser = userRepository.save(user);

        // Initialize UserStats
        UserStats stats = UserStats.builder()
                .user(savedUser)
                .currentStreak(0)
                .longestStreak(0)
                .totalStudyTimeSeconds(0)
                .xp(0)
                .confidenceScore(60)
                .build();
        userStatsRepository.save(stats);

        return ResponseEntity.ok(Map.of("message", "User registered successfully!"));
    }

    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        Optional<User> userOpt = userRepository.findByEmail(loginRequest.getEmail());

        if (userOpt.isEmpty() || !passwordEncoder.matches(loginRequest.getPassword(), userOpt.get().getPasswordHash())) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Error: Invalid email or password!"));
        }

        User user = userOpt.get();
        String jwt = tokenProvider.generateToken(user.getId(), user.getEmail(), user.getUsername());

        return ResponseEntity.ok(AuthResponse.builder()
                .token(jwt)
                .userId(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .learningLevel(user.getLearningLevel())
                .build());
    }

    @PostMapping("/google")
    public ResponseEntity<?> authenticateGoogleUser(@Valid @RequestBody GoogleLoginRequest googleLoginRequest) {
        // Check if user exists by Google ID or by Email
        Optional<User> userByGoogleId = userRepository.findByGoogleId(googleLoginRequest.getGoogleId());
        User user;

        if (userByGoogleId.isPresent()) {
            user = userByGoogleId.get();
        } else {
            Optional<User> userByEmail = userRepository.findByEmail(googleLoginRequest.getEmail());
            if (userByEmail.isPresent()) {
                // User exists with this email, link their Google ID
                user = userByEmail.get();
                user.setGoogleId(googleLoginRequest.getGoogleId());
                userRepository.save(user);
            } else {
                // Register a new user for Google Sign-In
                // Create a unique username from their name
                String baseUsername = googleLoginRequest.getName().toLowerCase().replaceAll("\\s+", "");
                String username = baseUsername;
                int suffix = 1;
                while (userRepository.existsByUsername(username)) {
                    username = baseUsername + suffix;
                    suffix++;
                }

                user = User.builder()
                        .username(username)
                        .email(googleLoginRequest.getEmail())
                        .googleId(googleLoginRequest.getGoogleId())
                        .learningLevel("B1")
                        .build();
                User savedUser = userRepository.save(user);

                // Initialize UserStats for Google registered user
                UserStats stats = UserStats.builder()
                        .user(savedUser)
                        .currentStreak(0)
                        .longestStreak(0)
                        .totalStudyTimeSeconds(0)
                        .xp(0)
                        .confidenceScore(60)
                        .build();
                userStatsRepository.save(stats);
                user = savedUser;
            }
        }

        String jwt = tokenProvider.generateToken(user.getId(), user.getEmail(), user.getUsername());

        return ResponseEntity.ok(AuthResponse.builder()
                .token(jwt)
                .userId(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .learningLevel(user.getLearningLevel())
                .build());
    }
}
