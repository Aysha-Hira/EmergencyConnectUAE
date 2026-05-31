package com.example.demo.incidents.controller;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.example.demo.incidents.dto.AssignmentRequestDTO;
import com.example.demo.incidents.model.Assignment;
import com.example.demo.incidents.model.Incident;
import com.example.demo.incidents.model.IncidentStatus;
import com.example.demo.incidents.service.IncidentService;

import jakarta.validation.Valid;


@RestController
@RequestMapping("/api/incidents")
public class IncidentController {

    private final IncidentService incidentService;

    public IncidentController(IncidentService incidentService) {
        this.incidentService = incidentService;
    }

    // GET /api/incidents?page=0&size=10&status=OPEN
    @GetMapping
    @PreAuthorize("hasAnyRole('DISPATCHER', 'RESPONDER')")

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
    public ResponseEntity<Incident> getById(@PathVariable String id) {
        return ResponseEntity.ok(incidentService.getById(id));
    }

    // POST /api/incidents
    @PostMapping
    @PreAuthorize("hasRole('DISPATCHER')")
    public ResponseEntity<Incident> create(@Valid @RequestBody Incident incident) {
        return ResponseEntity.status(HttpStatus.CREATED).body(incidentService.create(incident));
    }

    // PUT /api/incidents/{id}
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('DISPATCHER')")
    public ResponseEntity<Incident> update(@Valid @RequestBody Incident incident, @PathVariable String id) {
        return ResponseEntity.ok(incidentService.update(incident, id));
    }

    // PATCH /api/incidents/{id}/status
    // Body: { "status": "IN_PROGRESS" }
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('DISPATCHER')")
    public ResponseEntity<Incident> updateStatus(@PathVariable String id, @RequestParam IncidentStatus status) {
        return ResponseEntity.ok(incidentService.updateStatus(id, status));
    }

    // DELETE /api/incidents/{id}
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('DISPATCHER')")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        incidentService.delete(id);
        return ResponseEntity.noContent().build();
    }

    // --- Assignment endpoints ---

    // GET /api/incidents/{id}/assignments
    @GetMapping("/{id}/assignments")
    @PreAuthorize("hasAnyRole('DISPATCHER', 'RESPONDER')")
    public ResponseEntity<List<Assignment>> getAssignments(@PathVariable String id) {
        return ResponseEntity.ok(incidentService.getAssignments(id));
    }

    // POST /api/incidents/{id}/assignments
    @PostMapping("/{id}/assignments")
    @PreAuthorize("hasRole('DISPATCHER')")
    public ResponseEntity<Assignment> addAssignment(@PathVariable String id,
            @RequestBody AssignmentRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(incidentService.addAssignment(id, request));
    }

    // DELETE /api/incidents/{incidentId}/assignments/{assignmentId}
    @DeleteMapping("/{incidentId}/assignments/{assignmentId}")
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