package com.sarth.walletsim.service;

import com.sarth.walletsim.constants.UserRole;
import com.sarth.walletsim.dto.AuthRequest;
import com.sarth.walletsim.dto.AuthResponse;
import com.sarth.walletsim.dto.RegisterRequest;
import com.sarth.walletsim.entity.AppUser;
import com.sarth.walletsim.repository.AppUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AppUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public AuthResponse register(RegisterRequest request) {
        String email = normalizeEmail(request.getEmail());
        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Email is already registered");
        }

        AppUser user = new AppUser();
        user.setFullName(request.getFullName());
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setRole(UserRole.USER);

        AppUser savedUser = userRepository.save(user);
        return AuthResponse.from(savedUser, jwtService.generateToken(savedUser));
    }

    public AuthResponse login(AuthRequest request) {
        String email = normalizeEmail(request.getEmail());
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, request.getPassword())
        );

        AppUser user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Invalid email or password"));

        return AuthResponse.from(user, jwtService.generateToken(user));
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase();
    }
}
