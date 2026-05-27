package com.example.demo.users.service;

import com.example.demo.users.dto.AuthResponseDTO;
import com.example.demo.users.dto.LoginRequestDTO;
import com.example.demo.users.dto.RegisterRequestDTO;

public interface AuthService {
    AuthResponseDTO register(RegisterRequestDTO request);
    AuthResponseDTO login(LoginRequestDTO request, jakarta.servlet.http.HttpSession session);
    void logout(jakarta.servlet.http.HttpSession session);
}