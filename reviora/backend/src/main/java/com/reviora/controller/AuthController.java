package com.reviora.controller;

import com.reviora.dto.*;
import com.reviora.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "Auth endpoints")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    @Operation(summary = "Register a new user")
    public ResponseEntity<AuthResponseDto> register(@Valid @RequestBody RegisterDto req) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(authService.register(req.getName(), req.getEmail(), req.getPassword()));
    }

    @PostMapping("/login")
    @Operation(summary = "Login with email/password")
    public ResponseEntity<AuthResponseDto> login(@Valid @RequestBody LoginDto req) {
        return ResponseEntity.ok(authService.login(req.getEmail(), req.getPassword()));
    }

    @PostMapping("/google")
    @Operation(summary = "Login with Google OAuth")
    public ResponseEntity<AuthResponseDto> googleLogin(@Valid @RequestBody GoogleDto req) {
        return ResponseEntity.ok(authService.googleLogin(req.getCredential()));
    }

    @PostMapping("/refresh")
    @Operation(summary = "Refresh access token")
    public ResponseEntity<AuthResponseDto> refresh(@Valid @RequestBody RefreshDto req) {
        return ResponseEntity.ok(authService.refresh(req.getRefreshToken()));
    }

    @PostMapping("/logout")
    @Operation(summary = "Logout and invalidate refresh tokens")
    public ResponseEntity<Void> logout(Authentication auth) {
        if (auth != null) authService.logout(auth.getName());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/me")
    @Operation(summary = "Get current user profile")
    public ResponseEntity<UserDto> me(Authentication auth) {
        return ResponseEntity.ok(authService.getMe(auth.getName()));
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("OK");
    }

    /* ── Inner request DTOs ─────────────────────────── */
    @Data @NoArgsConstructor @AllArgsConstructor
    public static class RegisterDto {
        @NotBlank @Size(min=2,max=100) private String name;
        @NotBlank @Email               private String email;
        @NotBlank @Size(min=8)         private String password;
    }

    @Data @NoArgsConstructor @AllArgsConstructor
    public static class LoginDto {
        @NotBlank @Email        private String email;
        @NotBlank @Size(min=6)  private String password;
    }

    @Data @NoArgsConstructor @AllArgsConstructor
    public static class GoogleDto {
        @NotBlank private String credential;
    }

    @Data @NoArgsConstructor @AllArgsConstructor
    public static class RefreshDto {
        @NotBlank private String refreshToken;
    }
}
