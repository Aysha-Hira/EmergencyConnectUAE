package com.example.demo.incidents.service.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.example.demo.exceptions.ResourceNotFoundException;
import com.example.demo.incidents.dto.AssignmentRequestDTO;
import com.example.demo.incidents.model.Assignment;
import com.example.demo.incidents.model.AssignmentStatus;
import com.example.demo.incidents.model.Incident;
import com.example.demo.incidents.model.IncidentStatus;
import com.example.demo.incidents.repository.IncidentRepository;
import com.example.demo.incidents.service.IncidentService;

import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import java.util.concurrent.TimeUnit;

@Service
public class IncidentServiceImpl implements IncidentService {

    private final IncidentRepository incidentRepo;
    private final RedissonClient redissonClient;

    public IncidentServiceImpl(IncidentRepository incidentRepo, RedissonClient redissonClient) {
        this.incidentRepo = incidentRepo;
        this.redissonClient = redissonClient;
    }

    @Override
    public Incident create(Incident incident) {
        incident.setStatus(IncidentStatus.OPEN);
        
        LocalDateTime now = LocalDateTime.now();
        incident.setCreatedAt(now);
        incident.setUpdatedAt(now);
        
        // TODO: set reportedBy from JWT principal once security is configured
        return incidentRepo.save(incident);
    }

    @Override
    public Incident getById(String id) {
        return incidentRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Incident", "incidentId", id));
    }

    @Override
    public Page<Incident> getAll(Pageable pageable) {
        return incidentRepo.findAll(pageable);
    }

    @Override
    public Page<Incident> getByStatus(IncidentStatus status, Pageable pageable) {
        return incidentRepo.findByStatus(status, pageable);
    }

    @Override
    public Incident update(Incident incident, String id) {
        Incident existing = getById(id);

        if (incident.getTitle() != null) existing.setTitle(incident.getTitle());
        if (incident.getDescription() != null) existing.setDescription(incident.getDescription());
        if (incident.getType() != null) existing.setType(incident.getType());
        if (incident.getSeverity() != null) existing.setSeverity(incident.getSeverity());
        if (incident.getLocation() != null) existing.setLocation(incident.getLocation());
        if (incident.getLatitude() != null) existing.setLatitude(incident.getLatitude());
        if (incident.getLongitude() != null) existing.setLongitude(incident.getLongitude());

        existing.setUpdatedAt(LocalDateTime.now());
        // TODO: add AuditLog entry here once AuditLog is ready
        return incidentRepo.save(existing);
    }

    @Override
    public Incident updateStatus(String id, IncidentStatus newStatus) {
        Incident existing = getById(id);

        // Enforce valid status transitions only
        // OPEN -> IN_PROGRESS -> RESOLVED or CANCELLED
        
        // can't change after resolved or cancelled
        IncidentStatus current = existing.getStatus();
        if (current == IncidentStatus.RESOLVED || current == IncidentStatus.CANCELLED) {
            throw new RuntimeException("Cannot change status of a " + current + " incident");
        }

        existing.setStatus(newStatus);
        existing.setUpdatedAt(LocalDateTime.now());

        if (newStatus == IncidentStatus.RESOLVED) {
            existing.setResolvedAt(LocalDateTime.now());
        }

        // TODO: add AuditLog entry here once AuditLog is ready
        return incidentRepo.save(existing);
    }

    @Override
    public void delete(String id) {
        if (!incidentRepo.existsById(id))
            throw new ResourceNotFoundException("Incident", "incidentId", id);
        incidentRepo.deleteById(id);
    }

    @Override
    public Assignment addAssignment(String incidentId, AssignmentRequestDTO request) {
        // Tprevent two dispatchers being assigned the same unit to two different incidents at the same time
        String lockKey = "lock:unit:assign:" + request.getUnitId();
        RLock lock = redissonClient.getLock(lockKey);
        try {
            boolean acquired = lock.tryLock(5, 10, TimeUnit.SECONDS);
            if (!acquired) throw new RuntimeException("Could not acquire lock for unit " + request.getUnitId());

        Incident incident = getById(incidentId);

        // Check incident is still open or in progress — can't assign to resolved/cancelled
        if (incident.getStatus() == IncidentStatus.RESOLVED ||
            incident.getStatus() == IncidentStatus.CANCELLED) {
            throw new RuntimeException("Cannot assign unit to a " + incident.getStatus() + " incident");
        }

        // Check unit isn't already assigned to this incident
        boolean alreadyAssigned = incident.getAssignments().stream()
                .anyMatch(a -> a.getUnitId().equals(request.getUnitId())
                        && a.getStatus() == AssignmentStatus.ACTIVE);
        if (alreadyAssigned) {
            throw new RuntimeException("Unit " + request.getUnitId() + " is already assigned to this incident");
        }

        // Build the embedded assignment
        Assignment assignment = new Assignment();
        assignment.setId(UUID.randomUUID().toString());
        assignment.setUnitId(request.getUnitId());
        assignment.setUnitType(request.getUnitType());
        assignment.setAssignedBy(request.getAssignedBy()); // TODO: replace with JWT principal
        assignment.setStatus(AssignmentStatus.ACTIVE);
        assignment.setAssignedAt(LocalDateTime.now());
        assignment.setNotes(request.getNotes());

        // Move incident to IN_PROGRESS if it was OPEN
        if (incident.getStatus() == IncidentStatus.OPEN) {
            incident.setStatus(IncidentStatus.IN_PROGRESS);
        }

        incident.getAssignments().add(assignment);
        incident.setUpdatedAt(LocalDateTime.now());
        incidentRepo.save(incident);

        // TODO: add AuditLog entry here once AuditLog is ready
        // TODO: update unit status to DISPATCHED in unit collection once Unit model is ready
        
        return assignment;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Lock interrupted during unit assignment");
        } finally {
            if (lock.isHeldByCurrentThread()) lock.unlock();
        }
    }

    @Override
    public List<Assignment> getAssignments(String incidentId) {
        return getById(incidentId).getAssignments();
    }

    @Override
    public Assignment cancelAssignment(String incidentId, String assignmentId) {
        Incident incident = getById(incidentId);

        // Find the assignment inside the embedded list
        Assignment assignment = incident.getAssignments().stream()
                .filter(a -> a.getId().equals(assignmentId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Assignment", "assignmentId", assignmentId));

        if (assignment.getStatus() == AssignmentStatus.CANCELLED ||
            assignment.getStatus() == AssignmentStatus.COMPLETED) {
            throw new RuntimeException("Assignment is already " + assignment.getStatus());
        }

        assignment.setStatus(AssignmentStatus.CANCELLED);
        assignment.setCompletedAt(LocalDateTime.now());
        incident.setUpdatedAt(LocalDateTime.now());
        incidentRepo.save(incident);

        // TODO: update unit status back to AVAILABLE in unit collection once Unit model is ready
        // TODO: add AuditLog entry here once AuditLog is ready

        return assignment;
    }
}