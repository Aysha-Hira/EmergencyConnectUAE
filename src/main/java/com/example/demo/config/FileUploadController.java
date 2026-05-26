package com.example.demo.config;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;

@RestController
@RequestMapping("/api/files")
public class FileUploadController {
    private static final String UPLOAD_DIR = "/tmp/emergencyconnect/uploads/";

    // POST /api/files/upload
    @PostMapping("/upload")
    public ResponseEntity<String> upload(@RequestParam("file") MultipartFile file) throws IOException {

        String originalName = file.getOriginalFilename();
        if (originalName == null || originalName.isBlank()) {
            return ResponseEntity.badRequest().body("Invalid filename");
        }

        if (originalName.contains("..") || originalName.contains("/") || originalName.contains("\\")) {
            return ResponseEntity.badRequest().body("Invalid filename: directory traversal detected");
        }

        String lower = originalName.toLowerCase();
        if (!lower.endsWith(".pdf") && !lower.endsWith(".jpg") && !lower.endsWith(".png")) {
            return ResponseEntity.badRequest().body("File type not allowed");
        }

        Path uploadPath = Paths.get(UPLOAD_DIR);
        Files.createDirectories(uploadPath);
        uploadPath = uploadPath.toRealPath();
        
        Path resolvedPath = uploadPath.resolve(originalName).normalize();

        if (!resolvedPath.startsWith(uploadPath)) {
            return ResponseEntity.badRequest().body("Access denied: path traversal blocked");
        }

        Files.createDirectories(uploadPath);
        Files.copy(file.getInputStream(), resolvedPath, StandardCopyOption.REPLACE_EXISTING);

        return ResponseEntity.ok("File uploaded: " + resolvedPath.getFileName());
    }
}