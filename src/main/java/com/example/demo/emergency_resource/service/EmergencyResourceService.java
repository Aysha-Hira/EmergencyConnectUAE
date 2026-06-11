//Section 104 - Group 4
//Aysha Hira(1088000), Fatima Syed(1095190), Laisa Isra(1089635)


package com.example.demo.emergency_resource.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.example.demo.config.CachedPage;
import com.example.demo.emergency_resource.dto.ResourceRequestDTO;
import com.example.demo.emergency_resource.model.EmergencyResource;

public interface EmergencyResourceService {

	CachedPage<EmergencyResource> getAllResources(Pageable pageable);

	CachedPage<EmergencyResource> getAvailableResources(Pageable pageable);

	CachedPage<EmergencyResource> getAvailableResourcesPerHospital(Pageable pageable, String hospitalId);
	
    EmergencyResource addResource(ResourceRequestDTO resource);

    EmergencyResource updateResource(String resourceId, ResourceRequestDTO resourceDTO);

    // EmergencyResource updateResourceStatus(String resourceId, String status);

    void deleteResource(String resourceId);
}
