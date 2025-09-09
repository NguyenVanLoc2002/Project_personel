package com.fit.project_personel.services;

import com.fit.project_personel.dtos.reponses.auth.TokenResponse;
import com.fit.project_personel.dtos.requests.auth.LoginRequest;
import com.fit.project_personel.dtos.requests.auth.RefreshRequest;
import com.fit.project_personel.dtos.requests.auth.RegisterRequest;
import com.fit.project_personel.models.RefreshToken;
import com.fit.project_personel.models.Role;
import com.fit.project_personel.models.User;
import com.fit.project_personel.repositories.RefreshTokenRepository;
import com.fit.project_personel.repositories.RoleRepository;
import com.fit.project_personel.repositories.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    @Transactional
    public TokenResponse register(RegisterRequest request) {
       if(userRepository.existsByEmail(request.getEmail())) {
           throw new IllegalArgumentException("Email already in use");
       }

        Role defaultRole = roleRepository.findByName("USER")
                .orElseThrow(() -> new RuntimeException("Default role not found"));

        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .active(true)
                .roles(java.util.Set.of(defaultRole))
                .build();

        userRepository.save(user);

        return issueTokens(user);
    }

    private TokenResponse issueTokens(User user) {
        var roles = user.getRoles().stream().map(Role::getName).toList();
        var permissions = user.getRoles().stream()
                .flatMap(r -> r.getPermissions().stream())
                .map(p -> p.getName().name())
                .distinct()
                .toList();

        String accessToken = jwtService.generateAccessToken(user.getId(), user.getEmail(), roles, permissions);
        String refreshToken = jwtService.generateRefreshToken(user.getId(), user.getEmail());

        RefreshToken rt = RefreshToken.builder()
                .tokenHash(passwordEncoder.encode(refreshToken))
                .user(user)
                .expiryDate(Instant.now().plusSeconds(7 * 86400)) // 7 days
                .revoked(false)
                .build();

        refreshTokenRepository.save(rt);
        long expiresIn = 60L * 15; // 15 minutes
        return new TokenResponse(accessToken, refreshToken, "Bearer", expiresIn);
    }

    public TokenResponse login(LoginRequest request) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));
        return  issueTokens(user);
    }

    public TokenResponse refreshToken(RefreshRequest request) {
        var token = refreshTokenRepository.findByTokenHash(request.getRefreshToken())
                .orElseThrow(() -> new RuntimeException("Invalid refresh token"));

        if(token.isExpired() || token.getRevoked()) {
            throw new IllegalArgumentException("Refresh token expired or revoked");
        }

        token.setRevoked(true);
        refreshTokenRepository.save(token);

        User u = token.getUser();
        return issueTokens(u);
    }
}
