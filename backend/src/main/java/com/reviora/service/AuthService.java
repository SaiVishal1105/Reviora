package com.reviora.service;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.reviora.dto.*;
import com.reviora.exception.*;
import com.reviora.model.RefreshToken;
import com.reviora.model.User;
import com.reviora.repository.RefreshTokenRepository;
import com.reviora.repository.UserRepository;
import com.reviora.security.JwtTokenProvider;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.Collections;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;

    @Value("${google.client-id:}")
    private String googleClientId;

    @Value("${jwt.refresh-expiration}")
    private long refreshExpiration;

    private GoogleIdTokenVerifier googleVerifier;

    @PostConstruct
    public void init() {
        if (googleClientId != null && !googleClientId.isBlank()) {
            googleVerifier = new GoogleIdTokenVerifier.Builder(
                    new NetHttpTransport(),
                    GsonFactory.getDefaultInstance())
                    .setAudience(Collections.singletonList(googleClientId))
                    .build();
        }
    }

    @Transactional
    public AuthResponseDto register(String name, String email, String password) {
        if (userRepository.existsByEmail(email)) {
            throw new DuplicateResourceException("An account with this email already exists");
        }

        User user = User.builder()
                .name(name.trim())
                .email(email.toLowerCase().trim())
                .passwordHash(passwordEncoder.encode(password))
                .provider(User.AuthProvider.LOCAL)
                .role(User.Role.USER)
                .build();

        user = userRepository.save(user);
        return buildAuthResponse(user);
    }

    @Transactional
    public AuthResponseDto login(String email, String password) {
        User user = userRepository.findByEmail(email.toLowerCase().trim())
                .orElseThrow(() -> new ResourceNotFoundException("Invalid email or password"));

        if (!user.isActive()) {
            throw new InvalidTokenException("Account is deactivated");
        }

        if (user.getPasswordHash() == null ||
                !passwordEncoder.matches(password, user.getPasswordHash())) {
            throw new InvalidTokenException("Invalid email or password");
        }

        return buildAuthResponse(user);
    }

    @Transactional
    public AuthResponseDto googleLogin(String credential) {
        if (googleVerifier == null) {
            throw new ExecutionException("Google OAuth is not configured");
        }

        try {
            GoogleIdToken idToken = googleVerifier.verify(credential);
            if (idToken == null) throw new InvalidTokenException("Invalid Google credential");

            GoogleIdToken.Payload payload = idToken.getPayload();
            String googleId = payload.getSubject();
            String email    = payload.getEmail();
            String name     = (String) payload.get("name");
            String picture  = (String) payload.get("picture");

            User user = userRepository
                    .findByProviderAndProviderId(User.AuthProvider.GOOGLE, googleId)
                    .orElseGet(() -> userRepository.findByEmail(email)
                            .map(existing -> {
                                existing.setProviderId(googleId);
                                existing.setProvider(User.AuthProvider.GOOGLE);
                                if (existing.getPicture() == null) existing.setPicture(picture);
                                return userRepository.save(existing);
                            })
                            .orElseGet(() -> userRepository.save(User.builder()
                                    .name(name)
                                    .email(email)
                                    .picture(picture)
                                    .provider(User.AuthProvider.GOOGLE)
                                    .providerId(googleId)
                                    .role(User.Role.USER)
                                    .build()))
                    );

            return buildAuthResponse(user);
        } catch (InvalidTokenException e) {
            throw e;
        } catch (Exception e) {
            log.error("Google OAuth error: {}", e.getMessage());
            throw new InvalidTokenException("Failed to verify Google credential");
        }
    }

    @Transactional
    public AuthResponseDto refresh(String refreshTokenStr) {
        RefreshToken stored = refreshTokenRepository.findByToken(refreshTokenStr)
                .orElseThrow(() -> new InvalidTokenException("Invalid refresh token"));

        if (stored.isExpired()) {
            refreshTokenRepository.delete(stored);
            throw new InvalidTokenException("Refresh token has expired. Please log in again.");
        }

        User user = stored.getUser();
        String newAccessToken = jwtTokenProvider.generateAccessToken(
                user.getId(), user.getEmail(), user.getRole().name());

        return AuthResponseDto.builder()
                .accessToken(newAccessToken)
                .user(mapToUserDto(user))
                .build();
    }

    @Transactional
    public void logout(String userId) {
        try {
            refreshTokenRepository.deleteByUserId(java.util.UUID.fromString(userId));
        } catch (Exception e) {
            log.warn("Logout cleanup failed for user {}: {}", userId, e.getMessage());
        }
    }

    public UserDto getMe(String userId) {
        User user = userRepository.findById(java.util.UUID.fromString(userId))
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        return mapToUserDto(user);
    }

    // ── Internal helpers ──────────────────────────────

    private AuthResponseDto buildAuthResponse(User user) {
        String accessToken  = jwtTokenProvider.generateAccessToken(
                user.getId(), user.getEmail(), user.getRole().name());
        String refreshTokenStr = jwtTokenProvider.generateRefreshToken(user.getId());

        // Persist refresh token
        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .token(refreshTokenStr)
                .expiresAt(OffsetDateTime.now().plusSeconds(refreshExpiration / 1000))
                .build();
        refreshTokenRepository.save(refreshToken);

        return AuthResponseDto.builder()
                .user(mapToUserDto(user))
                .accessToken(accessToken)
                .refreshToken(refreshTokenStr)
                .build();
    }

    private UserDto mapToUserDto(User user) {
        return UserDto.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .picture(user.getPicture())
                .role(user.getRole().name())
                .createdAt(user.getCreatedAt())
                .build();
    }

    @Scheduled(cron = "0 0 3 * * *") // Run at 3 AM daily
    @Transactional
    public void cleanupExpiredTokens() {
        log.info("Cleaning up expired refresh tokens");
        refreshTokenRepository.deleteExpiredTokens(OffsetDateTime.now());
    }
}
