package com.example.demo.emergency_resource.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.example.demo.emergency_resource.dto.ResourceRequestDTO;
import com.example.demo.emergency_resource.model.EmergencyResource;

public interface EmergencyResourceService {

    Page<EmergencyResource> getAllResources(Pageable pageable);

    Page<EmergencyResource> getAvailableResources(Pageable pageable);

    Page<EmergencyResource> getAvailableResourcesPerHospital(Pageable pageable, String hospitalId);

    EmergencyResource addResource(ResourceRequestDTO resource);

    EmergencyResource updateResource(String resourceId, ResourceRequestDTO resourceDTO);

    // EmergencyResource updateResourceStatus(String resourceId, String status);

    void deleteResource(String resourceId);
}
