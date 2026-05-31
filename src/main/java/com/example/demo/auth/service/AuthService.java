package com.example.demo.auth.service;

import com.example.demo.auth.dto.AuthResponseDTO;
import com.example.demo.auth.dto.LoginRequestDTO;
import com.example.demo.auth.dto.RegisterRequestDTO;

public interface AuthService {
    AuthResponseDTO register(RegisterRequestDTO request);
    AuthResponseDTO login(LoginRequestDTO request, jakarta.servlet.http.HttpSession session);
    void logout(jakarta.servlet.http.HttpSession session);
}