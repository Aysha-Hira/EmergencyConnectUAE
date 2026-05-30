package com.example.demo.emergency_resource.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import com.example.demo.emergency_resource.model.EmergencyResource;
import com.example.demo.emergency_resource.model.ResourceStatus;

public interface EmergencyResourceRepository extends MongoRepository<EmergencyResource, String> {

    Page<EmergencyResource> findByStatus(ResourceStatus available, Pageable pageable);

    Page<EmergencyResource> findByStatusAndHospitalId(ResourceStatus available, String hospitalId, Pageable pageable);

}
