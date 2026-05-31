package com.example.demo.auth.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

// ADD THESE 2 IMPORTS:
import com.example.demo.audit.service.AuditService;
import com.example.demo.auth.dto.AuthResponseDTO;
import com.example.demo.auth.dto.MFASetupResponseDTO;
import com.example.demo.auth.dto.MFAVerifyRequestDTO;
import com.example.demo.auth.model.User;
import com.example.demo.auth.repository.UserRepository;
import com.example.demo.auth.service.MFAService;
import com.example.demo.config.JwtUtil;
import com.warrenstrange.googleauth.GoogleAuthenticatorKey;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth/mfa")
@RequiredArgsConstructor
@Tag(name = "MFA", description = "Multi-factor authentication setup and verification")
public class MfaController {

    private final MFAService mfaService;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    // ADD THESE 2 FIELDS:
    private final AuditService auditService;
    private final HttpServletRequest httpServletRequest;

    @PostMapping("/setup")
    @Operation(summary = "Generate MFA secret and QR code for a user")
    @PreAuthorize("hasAnyRole('DISPATCHER', 'HOSPITAL_ADMIN')")
    public ResponseEntity<MFASetupResponseDTO> setupMfa(@RequestParam String userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.isMfaEnabled()) {
            throw new RuntimeException("MFA is already enabled for this user");
        }

        GoogleAuthenticatorKey key = mfaService.generateCredentials();
        String secret = key.getKey();
        String qrUrl = mfaService.getQRCodeUrl(user.getEmail(), key);

        return ResponseEntity.ok(new MFASetupResponseDTO(
                secret,
                qrUrl,
                "Scan this QR code with Google Authenticator, then call /verify-setup"));
    }

    @PostMapping("/verify-setup")
    @Operation(summary = "Verify OTP code and enable MFA for a user")
    @PreAuthorize("hasAnyRole('DISPATCHER', 'HOSPITAL_ADMIN')")
    public ResponseEntity<String> verifyAndEnableMfa(
            @Valid @RequestBody MFAVerifyRequestDTO request,
            @RequestParam String secret) {

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.isMfaEnabled()) {
            throw new RuntimeException("MFA is already enabled");
        }

        boolean isValid = mfaService.verifyCode(secret, request.getCode());

        if (!isValid) {
            // ADD THIS AUDIT LOG LINE:
            auditService.logMfa(user.getId(), user.getUsername(), user.getRole().name(), "ENABLE_FAILED", false,
                    "Invalid verification code");
            throw new RuntimeException("Invalid MFA code. Please try again.");
        }

        user.setMfaEnabled(true);
        user.setMfaSecret(secret);
        userRepository.save(user);

        // ADD THIS AUDIT LOG LINE:
        auditService.logMfa(user.getId(), user.getUsername(), user.getRole().name(), "ENABLE", true,
                "MFA enabled via Google Authenticator");

        return ResponseEntity.ok("MFA enabled successfully! Next login will require code.");
    }

    @PostMapping("/verify-login")
    @Operation(summary = "Complete login by verifying MFA code (returns JWT)")
    public ResponseEntity<AuthResponseDTO> verifyLoginMfa(
            @Valid @RequestBody MFAVerifyRequestDTO request,
            HttpSession session) {

        String pendingUserId = (String) session.getAttribute("pendingMfaUserId");

        if (pendingUserId == null) {
            throw new RuntimeException("No pending MFA login. Please login first.");
        }

        if (!pendingUserId.equals(request.getUserId())) {
            throw new RuntimeException("User ID mismatch");
        }

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!user.isMfaEnabled()) {
            throw new RuntimeException("MFA is not enabled for this user");
        }

        boolean isValid = mfaService.verifyCode(user.getMfaSecret(), request.getCode());

        if (!isValid) {
            // ADD THIS AUDIT LOG LINE:
            auditService.logMfa(user.getId(), user.getUsername(), user.getRole().name(), "VERIFY_FAILED", false,
                    "Invalid MFA code during login");
            throw new RuntimeException("Invalid MFA code. Please try again.");
        }

        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole().name());

        session.removeAttribute("pendingMfaUserId");
        session.removeAttribute("pendingMfaUsername");

        session.setAttribute("userId", user.getId());
        session.setAttribute("username", user.getUsername());
        session.setAttribute("role", user.getRole().name());

        // ADD THIS AUDIT LOG LINE:
        auditService.logMfa(user.getId(), user.getUsername(), user.getRole().name(), "VERIFY_SUCCESS", true,
                "MFA verification successful");

        return ResponseEntity.ok(new AuthResponseDTO(
                user.getId(),
                user.getUsername(),
                user.getRole().name(),
                session.getId(), // sessionId
                token, // token
                "Login complete with MFA"));
    }

    @PostMapping("/disable")
    @Operation(summary = "Disable MFA for a user")
    @PreAuthorize("hasAnyRole('DISPATCHER', 'HOSPITAL_ADMIN')")
    public ResponseEntity<String> disableMfa(@RequestParam String userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setMfaEnabled(false);
        user.setMfaSecret(null);
        userRepository.save(user);

        // ADD THIS AUDIT LOG LINE:
        auditService.logMfa(user.getId(), user.getUsername(), user.getRole().name(), "DISABLE", true, "MFA disabled");

        return ResponseEntity.ok("MFA disabled successfully");
    }

    @GetMapping("/status")
    @Operation(summary = "Check whether MFA is enabled for a user")
    @PreAuthorize("hasAnyRole('DISPATCHER', 'HOSPITAL_ADMIN', 'RESPONDER')")
    public ResponseEntity<Map<String, Object>> getMfaStatus(@RequestParam String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return ResponseEntity.ok(Map.of(
                "userId", user.getId(),
                "username", user.getUsername(),
                "mfaEnabled", user.isMfaEnabled()));
    }
}