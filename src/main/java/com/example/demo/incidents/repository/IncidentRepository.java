package com.example.demo.incidents.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import com.example.demo.incidents.model.Incident;
import com.example.demo.incidents.model.IncidentStatus;

public interface IncidentRepository extends MongoRepository<Incident, String> {
    Page<Incident> findAll(Pageable pageable);
    Page<Incident> findByStatus(IncidentStatus status, Pageable pageable);
    Page<Incident> findByType(String type, Pageable pageable);
}