package com.example.demo.emergency_resource.controller;

import java.util.List;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.emergency_resource.model.EmergencyResource;

@RestController
@RequestMapping("/api/resources")
public class EmergencyResourceController {

    /**
     * GET /api/resources — list medical/other resources
     * GET /api/resources/available — query available resources
     */

    public List<EmergencyResource> getAllResources() {
        // TODO: Get all resources with pagination and filtering by
        // type/status/incidentId, etc.";
        return null;
    }

    public List<EmergencyResource> getAvailableResources() {
        // TODO: Get all resources with status = AVAILABLE, optionally filter by type
        // (medical, etc.) and location (lat/lon + radius)
        return null;
    }

}
