package com.example.demo.incidents.service.impl;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.example.demo.emergency_unit.repository.EmergencyUnitRepository;
import com.example.demo.audit.service.AuditService;
import com.example.demo.dispatch.service.PriorityDispatchService;
import com.example.demo.emergency_unit.model.UnitStatus;
import com.example.demo.emergency_unit.repository.EmergencyUnitRepository;
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

@Service
public class IncidentServiceImpl implements IncidentService {

    private final IncidentRepository incidentRepo;
    private final EmergencyUnitRepository unitRepo;
    private final AuditService auditService;
    
    private final RedissonClient redissonClient;
    private final PriorityDispatchService priorityDispatchService;



    public IncidentServiceImpl(IncidentRepository incidentRepo, RedissonClient redissonClient, AuditService auditService,
            EmergencyUnitRepository unitRepo, PriorityDispatchService priorityDispatchService) {
        this.incidentRepo = incidentRepo;
        this.unitRepo = unitRepo;
        this.auditService = auditService;
        
        this.redissonClient = redissonClient;
        this.priorityDispatchService = priorityDispatchService;
    }

    @Override
    @CacheEvict(value = "activeIncidents", allEntries = true)
    public Incident create(Incident incident) {
        incident.setStatus(IncidentStatus.OPEN);

        LocalDateTime now = LocalDateTime.now();
        incident.setCreatedAt(now);
        incident.setUpdatedAt(now);
        String currentUserId = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        incident.setReportedBy(currentUserId);
        incident.setAssignedDispatcher(currentUserId);


        return incidentRepo.save(incident);
    }

    @Override
    public Incident getById(String id) {
        return incidentRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Incident", "incidentId", id));
    }

    @Override
    @Cacheable(value = "activeIncidents", key = "#pageable.pageNumber + '-' + #pageable.pageSize")
    public Page<Incident> getAll(Pageable pageable) {
        return incidentRepo.findAll(pageable);
    }

    @Override
    @Cacheable(value = "activeIncidents", key = "'status-' + #status + '-' + #pageable.pageNumber + '-' + #pageable.pageSize")
    public Page<Incident> getByStatus(IncidentStatus status, Pageable pageable) {
        return incidentRepo.findByStatus(status, pageable);
    }

    @Override
    @CacheEvict(value = "activeIncidents", allEntries = true)
    public Incident update(Incident incident, String id) {
        Incident existing = getById(id);

        if (incident.getTitle() != null)
            existing.setTitle(incident.getTitle());
        if (incident.getDescription() != null)
            existing.setDescription(incident.getDescription());
        if (incident.getType() != null)
            existing.setType(incident.getType());
        if (incident.getSeverity() != null)
            existing.setSeverity(incident.getSeverity());
        if (incident.getLocation() != null)
            existing.setLocation(incident.getLocation());
        if (incident.getLatitude() != null)
            existing.setLatitude(incident.getLatitude());
        if (incident.getLongitude() != null)
            existing.setLongitude(incident.getLongitude());

        existing.setUpdatedAt(LocalDateTime.now());
        
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String userId = (String) auth.getPrincipal();
        auditService.logUpdate(userId, userId, auth.getAuthorities().toString(), "INCIDENT", id, null, null, "Incident updated");
        
        return incidentRepo.save(existing);
    }

    @Override
    @CacheEvict(value = "activeIncidents", allEntries = true)
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

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String userId = (String) auth.getPrincipal();
        auditService.logStatusChange(userId, userId, auth.getAuthorities().toString(), "INCIDENT", id, current.name(), newStatus.name(), "Incident status changed");
        
        return incidentRepo.save(existing);
    }

    @Override
    @CacheEvict(value = "activeIncidents", allEntries = true)
    public void delete(String id) {
        if (!incidentRepo.existsById(id))
            throw new ResourceNotFoundException("Incident", "incidentId", id);
        incidentRepo.deleteById(id);
    }

    @Override
    public Assignment addAssignment(String incidentId, AssignmentRequestDTO request) {
        // To prevent two dispatchers being assigned the same unit to two different
        // incidents at the same time
        String lockKey = "lock:unit:assign:" + request.getUnitId();
        RLock lock = redissonClient.getLock(lockKey);
        try {
            boolean acquired = lock.tryLock(5, 10, TimeUnit.SECONDS);
            if (!acquired)
                throw new RuntimeException("Could not acquire lock for unit " + request.getUnitId());

            Incident incident = getById(incidentId);

            if (incident.getStatus() == IncidentStatus.RESOLVED ||
                    incident.getStatus() == IncidentStatus.CANCELLED) {
                throw new RuntimeException("Cannot assign unit to a " + incident.getStatus() + " incident");
            }

            boolean alreadyAssigned = incident.getAssignments().stream()
                    .anyMatch(a -> a.getUnitId().equals(request.getUnitId())
                            && a.getStatus() == AssignmentStatus.ACTIVE);
            if (alreadyAssigned) {
                throw new RuntimeException("Unit " + request.getUnitId() + " is already assigned to this incident");
            }

            Assignment assignment = new Assignment();
            assignment.setId(UUID.randomUUID().toString());
            assignment.setUnitId(request.getUnitId());
            assignment.setUnitType(request.getUnitType());
            String currentUserId = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            assignment.setAssignedBy(currentUserId);
            assignment.setStatus(AssignmentStatus.ACTIVE);
            assignment.setAssignedAt(LocalDateTime.now());
            assignment.setNotes(request.getNotes());

            if (incident.getStatus() == IncidentStatus.OPEN) {
                incident.setStatus(IncidentStatus.IN_PROGRESS);
            }

            incident.getAssignments().add(assignment);
            incident.setUpdatedAt(LocalDateTime.now());
            incidentRepo.save(incident);

        auditService.logAssignment(currentUserId, currentUserId, "DISPATCHER", "INCIDENT", incidentId, request.getUnitId(), "Unit assigned to incident");

        
        unitRepo.findById(request.getUnitId()).ifPresent(unit -> {
            unit.setStatus(UnitStatus.DISPATCHED);
            unitRepo.save(unit);
        });
        
        return assignment;

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Lock interrupted during unit assignment");
        } finally {
            if (lock.isHeldByCurrentThread())
                lock.unlock();
        }
    }

    @Override
    public List<Assignment> getAssignments(String incidentId) {
        return getById(incidentId).getAssignments();
    }

    @Override
    public Assignment cancelAssignment(String incidentId, String assignmentId) {
        Incident incident = getById(incidentId);

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

        unitRepo.findById(assignment.getUnitId()).ifPresent(unit -> {
            unit.setStatus(UnitStatus.AVAILABLE);
            unitRepo.save(unit);
        });
        
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String userId = (String) auth.getPrincipal();
        auditService.logUpdate(userId, userId, auth.getAuthorities().toString(),
                "INCIDENT", incidentId, null, null,
                "Assignment " + assignmentId + " cancelled, unit " + assignment.getUnitId() + " returned to AVAILABLE");

        return assignment;
    }
    
    @Override
    @Cacheable(value = "dashboardSummary", key = "'summary'")
    public Map<String, Object> getDashboardSummary() {
        Map<String, Object> summary = new HashMap<>();
        
        summary.put("totalIncidents", incidentRepo.count());
        summary.put("openIncidents", incidentRepo.findByStatus(IncidentStatus.OPEN, PageRequest.of(0, 1)).getTotalElements());
        summary.put("inProgressIncidents", incidentRepo.findByStatus(IncidentStatus.IN_PROGRESS, PageRequest.of(0, 1)).getTotalElements());
        summary.put("resolvedIncidents", incidentRepo.findByStatus(IncidentStatus.RESOLVED, PageRequest.of(0, 1)).getTotalElements());
        return summary;
    }
    
    @Override
    public Page<Incident> getByReporter(String reportedBy, Pageable pageable) {
        return incidentRepo.findByReportedBy(reportedBy, pageable);
    }
}