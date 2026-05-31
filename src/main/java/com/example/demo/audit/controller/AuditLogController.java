package com.example.demo.audit.controller;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.example.demo.audit.dto.AuditLogResponseDTO;
import com.example.demo.audit.service.AuditService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/audit")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('DISPATCHER', 'HOSPITAL_ADMIN')")
@Tag(name = "Audit Logs", description = "View incident history and audit logs")
public class AuditLogController {

    private final AuditService auditService;

    @GetMapping
    @Operation(summary = "Get all audit logs with pagination")
    public ResponseEntity<Page<AuditLogResponseDTO>> getAllLogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("timestamp").descending());
        return ResponseEntity.ok(auditService.getAll(pageable));
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Get logs for a specific user")
    public ResponseEntity<Page<AuditLogResponseDTO>> getLogsByUser(
            @PathVariable String userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("timestamp").descending());
        return ResponseEntity.ok(auditService.getByUserId(userId, pageable));
    }

    @GetMapping("/entity")
    @Operation(summary = "Get logs for a specific entity (incident, unit, resource, hospital)")
    public ResponseEntity<Page<AuditLogResponseDTO>> getLogsByEntity(
            @RequestParam String entityType,
            @RequestParam String entityId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("timestamp").descending());
        return ResponseEntity.ok(auditService.getByEntity(entityType, entityId, pageable));
    }

    @GetMapping("/incident/{incidentId}")
    @Operation(summary = "Get incident history (all logs for a specific incident)")
    public ResponseEntity<Page<AuditLogResponseDTO>> getIncidentHistory(
            @PathVariable String incidentId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("timestamp").descending());
        return ResponseEntity.ok(auditService.getByEntity("INCIDENT", incidentId, pageable));
    }

    @GetMapping("/action/{action}")
    @Operation(summary = "Get logs by action type")
    public ResponseEntity<Page<AuditLogResponseDTO>> getLogsByAction(
            @PathVariable String action,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("timestamp").descending());
        return ResponseEntity.ok(auditService.getByAction(action, pageable));
    }

    @GetMapping("/date-range")
    @Operation(summary = "Get logs within a date range")
    public ResponseEntity<Page<AuditLogResponseDTO>> getLogsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("timestamp").descending());
        return ResponseEntity.ok(auditService.getByDateRange(start, end, pageable));
    }

    @GetMapping("/search")
    @Operation(summary = "Search logs by keyword (username, action, or details)")
    public ResponseEntity<Page<AuditLogResponseDTO>> searchLogs(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("timestamp").descending());
        return ResponseEntity.ok(auditService.search(keyword, pageable));
    }
}