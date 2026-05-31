package com.example.demo.users.service.impl;

import java.time.LocalDateTime;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.demo.audit.service.AuditService;
import com.example.demo.config.JwtUtil;
import com.example.demo.users.dto.AuthResponseDTO;
import com.example.demo.users.dto.LoginRequestDTO;
import com.example.demo.users.dto.RegisterRequestDTO;
import com.example.demo.users.model.User;
import com.example.demo.users.repository.UserRepository;
import com.example.demo.users.service.AuthService;
import com.example.demo.users.service.MFAService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@Service
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepo;
    private final JwtUtil jwtUtil;
    private final MFAService mfaService;
    private final AuditService auditService;
    private final HttpServletRequest httpServletRequest;
    private final BCryptPasswordEncoder passwordEncoder;

    public AuthServiceImpl(UserRepository userRepo, JwtUtil jwtUtil, MFAService mfaService, 
                           AuditService auditService, HttpServletRequest httpServletRequest) {
        this.userRepo = userRepo;
        this.jwtUtil = jwtUtil;
        this.mfaService = mfaService;
        this.auditService = auditService;
        this.httpServletRequest = httpServletRequest;
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
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(request.getRole());
        user.setCreatedAt(LocalDateTime.now());
        user.setMfaEnabled(false);
        user.setMfaSecret(null);

        userRepo.save(user);

        return new AuthResponseDTO(user.getId(), user.getUsername(), user.getRole().name(), null, null, "Registered successfully");
    }

    @Override
    public AuthResponseDTO login(LoginRequestDTO request, HttpSession session) {
        String ipAddress = httpServletRequest.getRemoteAddr();
        
        User user = userRepo.findByUsername(request.getUsername())
                .orElse(null);

        if (user == null || !passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            auditService.logLogin(null, request.getUsername(), null, ipAddress, false, "Invalid username or password");
            throw new RuntimeException("Invalid username or password");
        }

        if (!user.isActive()) {
            auditService.logLogin(user.getId(), user.getUsername(), user.getRole().name(), ipAddress, false, "Account disabled");
            throw new RuntimeException("Account is disabled");
        }

        auditService.logLogin(user.getId(), user.getUsername(), user.getRole().name(), ipAddress, true, null);

        if (user.isMfaEnabled()) {
            session.setAttribute("pendingMfaUserId", user.getId());
            session.setAttribute("pendingMfaUsername", user.getUsername());
            session.setMaxInactiveInterval(300);
            
            return AuthResponseDTO.mfaRequired(user.getId(), 
                "MFA required. Please verify with your authenticator app.");
        }

        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole().name());

        session.setAttribute("userId", user.getId());
        session.setAttribute("username", user.getUsername());
        session.setAttribute("role", user.getRole().name());

        return new AuthResponseDTO(
                user.getId(),
                user.getUsername(),
                user.getRole().name(),
                session.getId(),
                token,         // add token field
                "Login successful"
        );
    }

    @Override
    public void logout(HttpSession session) {
        session.removeAttribute("pendingMfaUserId");
        session.removeAttribute("pendingMfaUsername");
        session.invalidate();
    }
}