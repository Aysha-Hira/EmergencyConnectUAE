package com.example.demo.hospitals.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.hospitals.dto.HospitalCapacityDTO;
import com.example.demo.hospitals.model.Hospital;
import com.example.demo.hospitals.model.HospitalStatus;
import com.example.demo.hospitals.service.HospitalService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/hospitals")
@Tag(name = "Hospitals", description = "Manage hospitals and capacity")
public class HospitalController {

    private final HospitalService hospitalService;

    public HospitalController(HospitalService hospitalService) {
        this.hospitalService = hospitalService;
    }

    // GET -- /api/hospitals?page=0&size=10&status=OPERATIONAL
    @GetMapping
    @Operation(summary = "Get all hospitals with optional status filter")
    @PreAuthorize("hasAnyRole('DISPATCHER', 'HOSPITAL_ADMIN')") // only dispatchers/admins can view all hospitals
    public ResponseEntity<Page<Hospital>> getAllHospitals(@RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size, @RequestParam(required = false) HospitalStatus status) {
        size = Math.min(size, 100);
        Pageable pageable = PageRequest.of(page, size, Sort.by("name").ascending());

        Page<Hospital> result;
        if (status != null) {
            result = hospitalService.getByStatus(status, pageable);
        } else {
            result = hospitalService.getAllHospitals(pageable);
        }

        return ResponseEntity.ok(result);
    }

    // GET -- /api/hospitals/{id}
    @GetMapping("/{id}")
    @Operation(summary = "Get a hospital by ID")
    @PreAuthorize("hasAnyRole('DISPATCHER', 'HOSPITAL_ADMIN')")
    public ResponseEntity<Hospital> getById(@PathVariable String id) {
        return ResponseEntity.ok(hospitalService.getById(id));
    }

    // GET -- /api/hospitals/{id}/capacity
    @GetMapping("/{id}/capacity")
    @Operation(summary = "Get current bed capacity for a hospital")
    @PreAuthorize("hasAnyRole('DISPATCHER', 'HOSPITAL_ADMIN')")
    public ResponseEntity<HospitalCapacityDTO> getCapacity(@PathVariable String id) {
        return ResponseEntity.ok(hospitalService.getCapacity(id));
    }

    // GET -- /api/hospitals/status/{status}?page=0&size=10
    @GetMapping("/status/{status}")
    @Operation(summary = "Get hospitals filtered by status")
    @PreAuthorize("hasAnyRole('DISPATCHER', 'HOSPITAL_ADMIN')")
    public ResponseEntity<Page<Hospital>> getByStatus(@PathVariable HospitalStatus status,
            @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {
        size = Math.min(size, 100);
        Pageable pageable = PageRequest.of(page, size, Sort.by("name").ascending());

        return ResponseEntity.ok(hospitalService.getByStatus(status, pageable));
    }

    // POST -- /api/hospitals
    @PostMapping
    @Operation(summary = "Create a new hospital")
    @PreAuthorize("hasAnyRole('HOSPITAL_ADMIN')")
    public ResponseEntity<Hospital> create(@Valid @RequestBody Hospital hospital) {
        return ResponseEntity.status(HttpStatus.CREATED).body(hospitalService.saveNew(hospital));
    }

    // PUT -- /api/hospitals/{id}
    @PutMapping("/{id}")
    @Operation(summary = "Update an existing hospital")
    @PreAuthorize("hasAnyRole('HOSPITAL_ADMIN')")
    public ResponseEntity<Hospital> update(@Valid @RequestBody Hospital hospital, @PathVariable String id) {
        return ResponseEntity.ok(hospitalService.update(hospital, id));
    }

    // DELETE -- /api/hospitals/{id}
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a hospital")
    @PreAuthorize("hasAnyRole('HOSPITAL_ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        hospitalService.delete(id);
        return ResponseEntity.noContent().build();
    }

    // POST -- /api/hospitals/{hospitalId}/assign/{incidentId}
    @PostMapping("/{hospitalId}/assign/{incidentId}")
    @Operation(summary = "Assign a hospital to an incident")
    @PreAuthorize("hasAnyRole('DISPATCHER')")
    public ResponseEntity<String> assign(@PathVariable String hospitalId, @PathVariable String incidentId) {
        return ResponseEntity.ok(hospitalService.assign(hospitalId, incidentId));
    }
}