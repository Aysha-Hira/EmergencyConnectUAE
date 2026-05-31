package com.example.demo.incidents.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import com.example.demo.incidents.model.Incident;
import com.example.demo.incidents.model.IncidentStatus;
import com.example.demo.incidents.model.IncidentType;

public interface IncidentRepository extends MongoRepository<Incident, String> {
    Page<Incident> findAll(Pageable pageable);
    Page<Incident> findByStatus(IncidentStatus status, Pageable pageable);
    Page<Incident> findByType(IncidentType type, Pageable pageable);
    Page<Incident> findByReportedBy(String reportedBy, Pageable pageable);
}