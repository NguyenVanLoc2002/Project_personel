package com.fit.project_personel.controllers;

import com.fit.project_personel.dtos.reponses.auth.TokenResponse;
import com.fit.project_personel.dtos.requests.auth.LoginRequest;
import com.fit.project_personel.dtos.requests.auth.RefreshRequest;
import com.fit.project_personel.dtos.requests.auth.RegisterRequest;
import com.fit.project_personel.services.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<TokenResponse> register(@Valid @RequestBody RegisterRequest request) {
        var response = authService.register(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/refresh")
    public ResponseEntity<TokenResponse> refresh(@Valid @RequestBody RefreshRequest refreshToken) {
        return ResponseEntity.ok(authService.refreshToken(refreshToken));
    }
}
