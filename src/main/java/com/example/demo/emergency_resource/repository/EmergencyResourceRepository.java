package com.example.demo.emergency_resource.repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.example.demo.emergency_resource.model.EmergencyResource;

public interface EmergencyResourceRepository extends MongoRepository<EmergencyResource, String> {

}
