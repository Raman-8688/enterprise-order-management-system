package com.enterprise.oms.auth.controller;

import com.enterprise.oms.auth.dto.request.LoginRequest;
import com.enterprise.oms.auth.dto.request.RefreshTokenRequest;
import com.enterprise.oms.auth.dto.request.RegisterRequest;
import com.enterprise.oms.auth.dto.response.AuthResponse;
import com.enterprise.oms.auth.dto.response.UserResponse;
import com.enterprise.oms.auth.dto.response.ValidationResponse;
import com.enterprise.oms.auth.service.JwtService;
import com.enterprise.oms.auth.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(userService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(userService.login(request));
    }

    @PostMapping("/validate")
    public ResponseEntity<ValidationResponse> validateToken(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.substring(7);
        String email = jwtService.extractEmail(token);
        UserDetails userDetails = userDetailsService.loadUserByUsername(email);
        boolean isValid = jwtService.isTokenValid(token, userDetails);

        return ResponseEntity.ok(ValidationResponse.builder()
                .valid(isValid)
                .email(email)
                .message(isValid ? "Token is valid" : "Token is invalid")
                .build());
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refreshToken(@RequestBody RefreshTokenRequest request) {
        String email = jwtService.extractEmail(request.getRefreshToken());
        UserDetails userDetails = userDetailsService.loadUserByUsername(email);

        if (jwtService.isTokenValid(request.getRefreshToken(), userDetails)) {
            String newAccessToken = jwtService.generateToken(userDetails);
            return ResponseEntity.ok(AuthResponse.builder()
                    .accessToken(newAccessToken)
                    .refreshToken(request.getRefreshToken())
                    .email(email)
                    .message("Token refreshed successfully")
                    .build());
        }
        throw new RuntimeException("Invalid refresh token");
    }

    @GetMapping("/user/{email}")
    public ResponseEntity<UserResponse> getUser(@PathVariable String email) {
        return ResponseEntity.ok(userService.getUserByEmail(email));
    }
}