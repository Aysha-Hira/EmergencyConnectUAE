//Section 104 - Group 4
//Aysha Hira(1088000), Fatima Syed(1095190), Laisa Isra(1089635)


package com.example.demo.config;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/files")
@Tag(name = "File Upload", description = "Upload incident-related documents and images")
public class FileUploadController {
    private static final String UPLOAD_DIR = "/tmp/emergencyconnect/uploads/";

    // POST /api/files/upload
    @PostMapping("/upload")
    @Operation(summary = "Upload a file (PDF, JPG, or PNG only)")
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