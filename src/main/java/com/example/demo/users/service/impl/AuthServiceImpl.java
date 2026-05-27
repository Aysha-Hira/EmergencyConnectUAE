package com.example.demo.users.service.impl;

import java.time.LocalDateTime;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.demo.exceptions.ResourceNotFoundException;
import com.example.demo.users.dto.AuthResponseDTO;
import com.example.demo.users.dto.LoginRequestDTO;
import com.example.demo.users.dto.RegisterRequestDTO;
import com.example.demo.users.model.User;
import com.example.demo.users.repository.UserRepository;
import com.example.demo.users.service.AuthService;

import jakarta.servlet.http.HttpSession;

@Service
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepo;
    private final BCryptPasswordEncoder passwordEncoder;

    public AuthServiceImpl(UserRepository userRepo) {
        this.userRepo = userRepo;
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    @Override
    public AuthResponseDTO register(RegisterRequestDTO request) {
        if (userRepo.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username already taken");
        }
        if (userRepo.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already registered");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword())); // never store plaintext
        user.setRole(request.getRole());
        user.setCreatedAt(LocalDateTime.now());

        userRepo.save(user);

        return new AuthResponseDTO(user.getId(), user.getUsername(),
                user.getRole().name(), null, "Registered successfully");
    }

    @Override
    public AuthResponseDTO login(LoginRequestDTO request, HttpSession session) {
        User user = userRepo.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("Invalid username or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid username or password");
        }

        if (!user.isActive()) {
            throw new RuntimeException("Account is disabled");
        }

        // Store session data in Redis — Spring Session handles this automatically
        // because we added @EnableRedisHttpSession to SecurityConfig
        session.setAttribute("userId", user.getId());
        session.setAttribute("username", user.getUsername());
        session.setAttribute("role", user.getRole().name());

        // TODO: if user has MFA enabled, don't complete login here —
        // return a pending state and require MFA verification first

        return new AuthResponseDTO(
                user.getId(),
                user.getUsername(),
                user.getRole().name(),
                session.getId(),
                "Login successful"
        );
    }

    @Override
    public void logout(HttpSession session) {
        // Invalidating the session deletes it from Redis automatically
        session.invalidate();
    }
}