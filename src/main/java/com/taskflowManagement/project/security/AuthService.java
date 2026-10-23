package com.taskflowManagement.project.service;

import com.taskflowManagement.project.dto.AuthResponse;
import com.taskflowManagement.project.dto.LoginRequest;
import com.taskflowManagement.project.dto.RegisterRequest;
import com.taskflowManagement.project.entity.User;
import com.taskflowManagement.project.repository.UserRepository;
import com.taskflowManagement.project.security.JwtService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new IllegalArgumentException("Email is already registered");
        }

        User user = new User(
                request.name(),
                request.email(),
                passwordEncoder.encode(request.password())
        );

        user = userRepository.save(user);
        return createResponse(user);
    }

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() ->
                        new IllegalArgumentException("Invalid email or password"));

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new IllegalArgumentException("Invalid email or password");
        }

        return createResponse(user);
    }

    private AuthResponse createResponse(User user) {
        return new AuthResponse(
                jwtService.generateToken(user.getEmail()),
                "Bearer",
                user.getId(),
                user.getName(),
                user.getEmail()
        );
    }
}
