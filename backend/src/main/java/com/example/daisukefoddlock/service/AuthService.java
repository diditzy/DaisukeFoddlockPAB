package com.example.daisukefoddlock.service;

import com.example.daisukefoddlock.dto.AuthResponse;
import com.example.daisukefoddlock.dto.LoginRequest;
import com.example.daisukefoddlock.dto.RegisterRequest;
import com.example.daisukefoddlock.entity.User;
import com.example.daisukefoddlock.entity.User.Role;
import com.example.daisukefoddlock.repository.UserRepository;
import com.example.daisukefoddlock.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email is already taken");
        }

        String role = request.getRole();
        if (role == null || role.trim().isEmpty()) {
            role = "CUSTOMER";
        }

        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(User.Role.valueOf(role.toUpperCase()))
                .build();

        User savedUser = userRepository.save(user);
        String jwtToken = jwtService.generateToken(savedUser.getEmail(), savedUser.getRole().name());

        return new AuthResponse(
                jwtToken,
                savedUser.getId().toString(),
                savedUser.getEmail(),
                savedUser.getRole().name()
        );
    }

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        String jwtToken = jwtService.generateToken(user.getEmail(), user.getRole().name());

        return new AuthResponse(
                jwtToken,
                user.getId().toString(),
                user.getEmail(),
                user.getRole().name()
        );
    }
}
