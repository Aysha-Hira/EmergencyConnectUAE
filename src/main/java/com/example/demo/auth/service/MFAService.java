package com.example.demo.auth.service;

import com.warrenstrange.googleauth.GoogleAuthenticator;
import com.warrenstrange.googleauth.GoogleAuthenticatorKey;
import com.warrenstrange.googleauth.GoogleAuthenticatorQRGenerator;
import org.springframework.stereotype.Service;

@Service
public class MFAService {

    private final GoogleAuthenticator googleAuthenticator;

    public MFAService() {
        this.googleAuthenticator = new GoogleAuthenticator();
    }

    /**
     * Generate a new MFA secret for a user (returns the secret key string)
     */
    public String generateSecret() {
        GoogleAuthenticatorKey key = googleAuthenticator.createCredentials();
        return key.getKey();
    }

    /**
     * Generate a full GoogleAuthenticatorKey object (includes secret + validation codes)
     */
    public GoogleAuthenticatorKey generateCredentials() {
        return googleAuthenticator.createCredentials();
    }

    /**
     * Generate QR code URL that user can scan with Google Authenticator
     * @param email User's email (shown in their authenticator app)
     * @param key The GoogleAuthenticatorKey object (not the secret string)
     * @return URL string that can be turned into QR code
     */
    public String getQRCodeUrl(String email, GoogleAuthenticatorKey key) {
        return GoogleAuthenticatorQRGenerator.getOtpAuthURL(
            "EmergencyConnectUAE",  // Issuer (your app name)
            email,                   // User identifier
            key                      // The GoogleAuthenticatorKey object
        );
    }

    /**
     * Alternative: Generate QR code URL using just the secret string
     * This creates a GoogleAuthenticatorKey object internally
     */
    public String getQRCodeUrlFromSecret(String email, String secret) {
        // Create a GoogleAuthenticatorKey object from the secret
        GoogleAuthenticatorKey key = new GoogleAuthenticatorKey.Builder(secret)
            .setVerificationCode(0)
            .build();
        return GoogleAuthenticatorQRGenerator.getOtpAuthURL(
            "EmergencyConnectUAE",
            email,
            key
        );
    }

    /**
     * Verify the 6-digit code entered by user
     * @param secret The user's MFA secret (string)
     * @param code The 6-digit code from authenticator app
     * @return true if code is valid, false otherwise
     */
    public boolean verifyCode(String secret, int code) {
        return googleAuthenticator.authorize(secret, code);
    }

    /**
     * Verify with a small time window (more forgiving, less secure)
     */
    public boolean verifyCodeWithWindow(String secret, int code, int window) {
        return googleAuthenticator.authorize(secret, code, window);
    }
}