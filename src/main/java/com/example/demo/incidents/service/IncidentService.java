package com.example.demo.incidents.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.example.demo.incidents.dto.AssignmentRequestDTO;
import com.example.demo.incidents.model.Assignment;
import com.example.demo.incidents.model.Incident;
import com.example.demo.incidents.model.IncidentStatus;

import java.util.List;

public interface IncidentService {

	Page<Incident> getAll(Pageable pageable);
	
    // CRUD
    Incident create(Incident incident);
    Incident getById(String id);
    Incident update(Incident incident, String id);
    void delete(String id);
    
    //Custom(for incidents)
    Incident updateStatus(String id, IncidentStatus newStatus);
    Page<Incident> getByStatus(IncidentStatus status, Pageable pageable);

    // Assignments (embedded in incidents)
    Assignment addAssignment(String incidentId, AssignmentRequestDTO request);
    List<Assignment> getAssignments(String incidentId);
    Assignment cancelAssignment(String incidentId, String assignmentId);
}