package com.example.demo.incidents.controller;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.incidents.dto.AssignmentRequestDTO;
import com.example.demo.incidents.model.Assignment;
import com.example.demo.incidents.model.Incident;
import com.example.demo.incidents.model.IncidentStatus;
import com.example.demo.incidents.service.IncidentService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/incidents")
@Tag(name = "Incidents", description = "Report and manage emergency incidents")
public class IncidentController {

    private final IncidentService incidentService;

    public IncidentController(IncidentService incidentService) {
        this.incidentService = incidentService;
    }

    // GET /api/incidents?page=0&size=10&status=OPEN
    @GetMapping
    @PreAuthorize("hasAnyRole('DISPATCHER', 'RESPONDER')")
    @Operation(summary = "Get all incidents with optional status filter")
    public ResponseEntity<Page<Incident>> getAll(@RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size, @RequestParam(required = false) IncidentStatus status) {

        size = Math.min(size, 100);
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        Page<Incident> result;
        if (status != null) {
            result = incidentService.getByStatus(status, pageable);
        } else {
            result = incidentService.getAll(pageable);
        }

        return ResponseEntity.ok(result);
    }

    // GET /api/incidents/{id}
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('DISPATCHER', 'RESPONDER')")
    @Operation(summary = "Get an incident by ID")
    public ResponseEntity<Incident> getById(@PathVariable String id) {
        return ResponseEntity.ok(incidentService.getById(id));
    }

    // POST /api/incidents
    @PostMapping
    @Operation(summary = "Create a new incident")
    @PreAuthorize("hasRole('DISPATCHER')")
    public ResponseEntity<Incident> create(@Valid @RequestBody Incident incident) {
        return ResponseEntity.status(HttpStatus.CREATED).body(incidentService.create(incident));
    }

    // PUT /api/incidents/{id}
    @PutMapping("/{id}")
    @Operation(summary = "Update an existing incident")
    @PreAuthorize("hasRole('DISPATCHER')")
    public ResponseEntity<Incident> update(@Valid @RequestBody Incident incident, @PathVariable String id) {
        return ResponseEntity.ok(incidentService.update(incident, id));
    }

    // PATCH /api/incidents/{id}/status
    @PatchMapping("/{id}/status")
    @Operation(summary = "Update incident status (e.g. OPEN → IN_PROGRESS → RESOLVED)")
    @PreAuthorize("hasRole('DISPATCHER')")
    public ResponseEntity<Incident> updateStatus(@PathVariable String id, @RequestParam IncidentStatus status) {
        return ResponseEntity.ok(incidentService.updateStatus(id, status));
    }

    // DELETE /api/incidents/{id}
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete an incident")
    @PreAuthorize("hasRole('DISPATCHER')")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        incidentService.delete(id);
        return ResponseEntity.noContent().build();
    }

    // --- Assignment endpoints ---

    // GET /api/incidents/{id}/assignments
    @GetMapping("/{id}/assignments")
    @PreAuthorize("hasAnyRole('DISPATCHER', 'RESPONDER')")
    @Operation(summary = "Get all unit assignments for an incident")
    public ResponseEntity<List<Assignment>> getAssignments(@PathVariable String id) {
        return ResponseEntity.ok(incidentService.getAssignments(id));
    }

    // POST /api/incidents/{id}/assignments
    @PostMapping("/{id}/assignments")
    @Operation(summary = "Assign an emergency unit to an incident")
    @PreAuthorize("hasRole('DISPATCHER')")
    public ResponseEntity<Assignment> addAssignment(@PathVariable String id,
            @RequestBody AssignmentRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(incidentService.addAssignment(id, request));
    }

    // DELETE /api/incidents/{incidentId}/assignments/{assignmentId}
    @DeleteMapping("/{incidentId}/assignments/{assignmentId}")
    @Operation(summary = "Cancel a unit assignment from an incident")
    @PreAuthorize("hasRole('DISPATCHER')")
    public ResponseEntity<Assignment> cancelAssignment(@PathVariable String incidentId,
            @PathVariable String assignmentId) {
        return ResponseEntity.ok(incidentService.cancelAssignment(incidentId, assignmentId));
    }
    
 // GET /api/incidents/by-dispatcher?username=john&page=0&size=10
    @GetMapping("/by-dispatcher")
    @PreAuthorize("hasAnyRole('DISPATCHER', 'RESPONDER')")
    public ResponseEntity<Page<Incident>> getByReporter(
            @RequestParam String username,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        size = Math.min(size, 100);
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return ResponseEntity.ok(incidentService.getByReporter(username, pageable));
    }
}