//Section 104 - Group 4
//Aysha Hira(1088000), Fatima Syed(1095190), Laisa Isra(1089635)


package com.example.demo.dispatch.scheduler;

import java.util.concurrent.TimeUnit;

import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.cache.CacheManager;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.example.demo.dispatch.service.PriorityDispatchService;
import com.example.demo.dispatch.service.UnitLocatorService;
import com.example.demo.emergency_unit.model.EmergencyUnit;
import com.example.demo.emergency_unit.model.UnitStatus;
import com.example.demo.emergency_unit.repository.EmergencyUnitRepository;
import com.example.demo.emergency_unit.service.EmergencyUnitService;
import com.example.demo.incidents.model.Incident;
import com.example.demo.incidents.model.IncidentStatus;
import com.example.demo.incidents.repository.IncidentRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@EnableScheduling
@RequiredArgsConstructor
public class DispatchScheduler {

    private final PriorityDispatchService priorityDispatchService;
    private final UnitLocatorService unitLocatorService;
    private final IncidentRepository incidentRepository;
    private final EmergencyUnitRepository unitRepository;
    private final EmergencyUnitService emergencyUnitService;
    private final RedissonClient redissonClient;
    private final CacheManager cacheManager;
    
    /**
     * Runs every 5 seconds to check for pending incidents
     * Only ONE server instance can run this at a time (distributed lock)
     */
    @Scheduled(fixedDelay = 5000)  // Run every 5 seconds
    public void processDispatchQueue() {
        // Distributed lock to prevent multiple servers processing same incident
        RLock lock = redissonClient.getLock("lock:dispatch:scheduler");
        
        try {
            // Try to acquire lock with 2 second wait, hold for 4 seconds
            boolean acquired = lock.tryLock(2, 4, TimeUnit.SECONDS);
            
            if (!acquired) {
                log.debug("Another server is processing dispatch queue, skipping...");
                return;
            }
            
            // Get highest priority incident from queue
            String incidentId = priorityDispatchService.getHighestPriorityIncident();
            
            if (incidentId == null) {
                // No pending incidents
                return;
            }
            
            log.info("Processing highest priority incident: {}", incidentId);
            
            // Fetch incident from database
            Incident incident = incidentRepository.findById(incidentId).orElse(null);
            
            if (incident == null) {
                log.warn("Incident {} not found in database, removing from queue", incidentId);
                return;
            }
            
            // Skip if incident is already resolved or cancelled
            if (incident.getStatus() == IncidentStatus.RESOLVED || 
                incident.getStatus() == IncidentStatus.CANCELLED) {
                log.info("Incident {} is already resolved/cancelled, skipping", incidentId);
                return;
            }
            
            // Find nearest available unit
            if (incident.getLatitude() == null || incident.getLongitude() == null) {
                log.warn("Incident {} has no location coordinates", incidentId);
                return;
            }
            
            EmergencyUnit nearestUnit;
            
            // Use emirate-based dispatch if emirates field exists
            if (incident.getEmirates() != null && !incident.getEmirates().isEmpty()) {
                log.info("Using emirate-based dispatch for incident in {}", incident.getEmirates());
                nearestUnit = unitLocatorService.findNearestAvailableUnitInEmirate(
                    incident.getLatitude(),
                    incident.getLongitude(),
                    incident.getEmirates()
                );
            } else {
                log.info("No emirate specified, using distance-based dispatch");
                nearestUnit = unitLocatorService.findNearestAvailableUnit(
                    incident.getLatitude(),
                    incident.getLongitude()
                );
            }
            
            if (nearestUnit == null) {
                log.warn("No available units for incident {}", incidentId);
                // Re-add to queue to try again later
                priorityDispatchService.addToQueue(incident);
                return;
            }
            
            // Calculate and log estimated response time
            double distance = unitLocatorService.calculateDistance(
                incident.getLatitude(), incident.getLongitude(),
                nearestUnit.getLatitude(), nearestUnit.getLongitude()
            );
            String estimatedTime = unitLocatorService.getEstimatedResponseTime(distance, 
                incident.getEmirates() != null ? incident.getEmirates() : "unknown");
            log.info("Estimated response time for unit {}: {}", nearestUnit.getUnitName(), estimatedTime);
            
            // Assign the unit (with distributed lock on the unit)
            assignUnitWithLock(incident, nearestUnit);
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Dispatch scheduler interrupted", e);
        } catch (Exception e) {
            log.error("Error in dispatch scheduler", e);
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }
    
    private void assignUnitWithLock(Incident incident, EmergencyUnit unit) {
        String lockKey = "lock:unit:dispatch:" + unit.getId();
        RLock lock = redissonClient.getLock(lockKey);
        
        try {
            boolean acquired = lock.tryLock(3, 5, TimeUnit.SECONDS);
            
            if (!acquired) {
                log.warn("Unit {} is being assigned elsewhere, retrying later", unit.getId());
                // Re-add incident to queue
                priorityDispatchService.addToQueue(incident);
                return;
            }
            
            // Double-check unit is still available
            EmergencyUnit freshUnit = unitRepository.findById(unit.getId()).orElse(null);
            if (freshUnit == null || freshUnit.getStatus() != UnitStatus.AVAILABLE) {
                log.warn("Unit {} is no longer available", unit.getId());
                priorityDispatchService.addToQueue(incident);
                return;
            }
            
            // Assign unit to incident
            freshUnit.setStatus(UnitStatus.DISPATCHED);
            freshUnit.setCurrentIncidentId(incident.getId());
            unitRepository.save(freshUnit);
            
            // Update incident status
            incident.setStatus(IncidentStatus.IN_PROGRESS);
            incidentRepository.save(incident);
            
            cacheManager.getCache("units").clear();
            cacheManager.getCache("activeIncidents").clear();
            
            log.info("✅ ASSIGNED: Unit {} → Incident {} (Priority: {})", 
                     freshUnit.getUnitName(), incident.getId(), incident.getSeverity());
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Lock interrupted for unit assignment", e);
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }
    
    @Scheduled(fixedDelay = 60000)
    public void expireReservations() {
        emergencyUnitService.expireReservations();
    }
}