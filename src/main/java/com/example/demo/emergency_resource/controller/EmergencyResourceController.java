package com.example.demo.emergency_resource.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.config.CachedPage;
import com.example.demo.emergency_resource.dto.ResourceRequestDTO;
import com.example.demo.emergency_resource.model.EmergencyResource;
import com.example.demo.emergency_resource.service.EmergencyResourceService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/resources")
@Tag(name = "Emergency Resources", description = "Manage medical and emergency resources")
public class EmergencyResourceController {

    private final EmergencyResourceService emergencyResourceService;

    @GetMapping
    @Operation(summary = "Get all resources")
    public ResponseEntity<Page<EmergencyResource>> getAllResources(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        CachedPage<EmergencyResource> cached = emergencyResourceService.getAllResources(PageRequest.of(page, size));
        Page<EmergencyResource> result = new PageImpl<>(
            cached.getContent(),
            PageRequest.of(cached.getPageNumber(), cached.getPageSize() == 0 ? 1 : cached.getPageSize()),
            cached.getTotalElements()
        );
        return ResponseEntity.ok(result);
    }

    @GetMapping("/available")
    @Operation(summary = "Get all available resources")
    public ResponseEntity<Page<EmergencyResource>> getAvailableResources(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        CachedPage<EmergencyResource> cached = emergencyResourceService.getAvailableResources(PageRequest.of(page, size));
        Page<EmergencyResource> result = new PageImpl<>(
            cached.getContent(),
            PageRequest.of(cached.getPageNumber(), cached.getPageSize() == 0 ? 1 : cached.getPageSize()),
            cached.getTotalElements()
        );
        return ResponseEntity.ok(result);
    }

    @GetMapping("/available/hospital")
    @Operation(summary = "Get available resources by hospital")
    public ResponseEntity<Page<EmergencyResource>> getAvailableResourcesPerHospital(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam String hospitalId) {
        CachedPage<EmergencyResource> cached = emergencyResourceService.getAvailableResourcesPerHospital(PageRequest.of(page, size), hospitalId);
        Page<EmergencyResource> result = new PageImpl<>(
            cached.getContent(),
            PageRequest.of(cached.getPageNumber(), cached.getPageSize() == 0 ? 1 : cached.getPageSize()),
            cached.getTotalElements()
        );
        return ResponseEntity.ok(result);
    }

    @PostMapping
    @Operation(summary = "Add a new resource")
    public ResponseEntity<EmergencyResource> addResource(@Valid @RequestBody ResourceRequestDTO resourceDTO) {
        return ResponseEntity.ok(emergencyResourceService.addResource(resourceDTO));
    }

    @PutMapping("/{resourceId}")
    @Operation(summary = "Update a resource")
    public ResponseEntity<EmergencyResource> updateResource(
            @PathVariable String resourceId,
            @Valid @RequestBody ResourceRequestDTO resourceDTO) {
        return ResponseEntity.ok(emergencyResourceService.updateResource(resourceId, resourceDTO));
    }

    @DeleteMapping("/{resourceId}")
    @Operation(summary = "Delete a resource")
    public ResponseEntity<Void> deleteResource(@PathVariable String resourceId) {
        emergencyResourceService.deleteResource(resourceId);
        return ResponseEntity.noContent().build();
    }
}