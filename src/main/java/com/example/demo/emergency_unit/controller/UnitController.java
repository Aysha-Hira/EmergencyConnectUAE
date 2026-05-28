package com.example.demo.emergency_unit.controller;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.emergency_unit.model.EmergencyUnit;
import com.example.demo.emergency_unit.model.UnitStatus;
import com.example.demo.emergency_unit.service.EmergencyUnitService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/units")
@RequiredArgsConstructor
@Tag(name = "Emergency Units", description = "Manage and query emergency units")
public class UnitController {

    private final EmergencyUnitService emergencyUnitService;

    @GetMapping
    @Operation(summary = "Get all emergency units")
    @Cacheable(value = "units", key = "'all'")
    public ResponseEntity<Page<EmergencyUnit>> getAllUnits(@RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size, @RequestParam(required = false) UnitStatus status) {
        size = Math.min(size, 100);
        Pageable pageable = PageRequest.of(page, size, Sort.by("unitName").ascending());

        Page<EmergencyUnit> result;
        if (status != null && status.equals(UnitStatus.AVAILABLE)) {
            result = emergencyUnitService.getAvailableUnits(pageable);
        } else {
            result = emergencyUnitService.getAllUnits(pageable);
        }
        return ResponseEntity.ok(result);
    }

    @GetMapping("/available")
    @Operation(summary = "Get all available units")
    @Cacheable(value = "units", key = "'available'")
    public ResponseEntity<Page<EmergencyUnit>> getAvailableUnits(@RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        size = Math.min(size, 100);
        Pageable pageable = PageRequest.of(page, size, Sort.by("unitName").ascending());

        return ResponseEntity.ok(emergencyUnitService.getAvailableUnits(pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a unit by ID")
    @Cacheable(value = "units", key = "#id")
    public ResponseEntity<EmergencyUnit> getUnitById(@PathVariable String id) {
        return ResponseEntity.ok(emergencyUnitService.getUnitById(id));
    }

    @GetMapping("/name")
    @Cacheable(value = "units", key = "'unit:' + #name")
    public ResponseEntity<EmergencyUnit> getUnitByName(@RequestParam String name) {
        return ResponseEntity.ok(emergencyUnitService.getUnitByName(name));
    }

    @GetMapping("/name/id")
    @Cacheable(value = "units", key = "'unitId:' + #name")
    public ResponseEntity<String> getUnitIdByName(@RequestParam String name) {
        return ResponseEntity.ok(emergencyUnitService.getIdByName(name));
    }

    @PutMapping("/{id}/status")
    @Operation(summary = "Update a unit's status")
    public ResponseEntity<EmergencyUnit> updateUnitStatus(@PathVariable String id,
            @RequestParam UnitStatus status) {
        return ResponseEntity.ok(emergencyUnitService.updateUnitStatus(id, status));
    }
}