package com.example.demo.hospitals.service.impl;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.RedisTemplate;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import org.springframework.stereotype.Service;

import com.example.demo.hospitals.service.HospitalService;
import com.example.demo.incidents.model.Incident;
import com.example.demo.incidents.repository.IncidentRepository;
import com.example.demo.hospitals.repository.HospitalRepository;

import com.example.demo.hospitals.model.Hospital;
import com.example.demo.hospitals.model.HospitalStatus;
import com.example.demo.hospitals.dto.HospitalCapacityDTO;
import com.example.demo.emergency_unit.repository.EmergencyUnitRepository;
import com.example.demo.exceptions.ResourceNotFoundException;

@Service
public class HospitalServiceImpl implements HospitalService {
    private final IncidentRepository incidentRepo;
    private final HospitalRepository hospitalRepo;
    private final EmergencyUnitRepository unitRepo;
    private final RedisTemplate<String, Object> redisTemplate;
    private final RedissonClient redissonClient;

    public HospitalServiceImpl(IncidentRepository incidentRepo, HospitalRepository hospitalRepo, EmergencyUnitRepository unitRepo,
            RedisTemplate<String, Object> redisTemplate, RedissonClient redissonClient) {
        this.incidentRepo = incidentRepo;
        this.hospitalRepo = hospitalRepo;
        this.unitRepo = unitRepo;

        this.redisTemplate = redisTemplate;
        this.redissonClient = redissonClient;
    }

    @Override
    public Page<Hospital> getAllHospitals(Pageable pageable) {
        return hospitalRepo.findAll(pageable);
    }

    @Override
    public Hospital saveNew(Hospital h) {
        h.setLastUpdated(LocalDateTime.now());
        return hospitalRepo.save(h);
    }

    @Override
    public Hospital getById(String id) {
        return hospitalRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Hospital", "hospitalId", id));
    }

    @Override
    public Hospital update(Hospital h, String id) {
        Hospital existing = getById(id);

        if (h.getName() != null)
            existing.setName(h.getName());
        if (h.getAddress() != null)
            existing.setAddress(h.getAddress());

        if (h.getLatitude() != null)
            existing.setLatitude(h.getLatitude());
        if (h.getLongitude() != null)
            existing.setLongitude(h.getLongitude());

        if (h.getTotalBeds() != null)
            existing.setTotalBeds(h.getTotalBeds());
        if (h.getAvailableBeds() != null)
            existing.setAvailableBeds(h.getAvailableBeds());

        if (h.getIcuBeds() != null)
            existing.setIcuBeds(h.getIcuBeds());
        if (h.getAvailableIcuBeds() != null)
            existing.setAvailableIcuBeds(h.getAvailableIcuBeds());

        if (h.getErOperational() != null)
            existing.setErOperational(h.getErOperational());

        if (h.getResourceIds() != null)
            existing.setResourceIds(h.getResourceIds());

        if (h.getStatus() != null)
            existing.setStatus(h.getStatus());

        existing.setLastUpdated(LocalDateTime.now());

        redisTemplate.delete("cache:hospital:capacity:" + id);

        return hospitalRepo.save(existing);
    }

    @Override
    public void delete(String id) {
        if (!hospitalRepo.existsById(id))
            throw new ResourceNotFoundException("Hospital", "hospitalId", id);

        hospitalRepo.deleteById(id);
        redisTemplate.delete("cache:hospital:capacity:" + id);

    }

    @Override
    public HospitalCapacityDTO getCapacity(String id) {
        String cacheKey = "cache:hospital:capacity:" + id;

        // Check Redis cache first
        HospitalCapacityDTO cached = (HospitalCapacityDTO) redisTemplate.opsForValue().get(cacheKey);
        if (cached != null)
            return cached;

        // Cache miss — hit MongoDB
        Hospital hospital = getById(id);

        HospitalCapacityDTO dto = new HospitalCapacityDTO(
                hospital.getId(),
                hospital.getName(),
                hospital.getTotalBeds(),
                hospital.getAvailableBeds(),
                hospital.getIcuBeds(),
                hospital.getAvailableIcuBeds(),
                hospital.getErOperational(),
                hospital.getStatus());

        // Cache for 60 seconds — short TTL because capacity changes frequently
        redisTemplate.opsForValue().set(cacheKey, dto, Duration.ofSeconds(60));

        return dto;
    }

    @Override
    public String assign(String hospitalId, String incidentId) {

        String lockKey = "lock:hospital:assign:" + hospitalId;
        RLock lock = redissonClient.getLock(lockKey);

        try {
            // Try to acquire lock — wait up to 5s, hold for max 10s
            boolean acquired = lock.tryLock(5, 10, TimeUnit.SECONDS);
            if (!acquired) {
                throw new RuntimeException("Could not acquire lock for hospital " + hospitalId + " — try again");
            }

            // Re-fetch inside lock to get latest state (don't trust pre-lock data)
            Hospital hospital = getById(hospitalId);

            if (hospital.getAvailableBeds() == null || hospital.getAvailableBeds() <= 0) {
                throw new RuntimeException("No available beds at hospital " + hospitalId);
            }

            Incident incident = incidentRepo.findById(incidentId)
            		.orElseThrow(() -> new ResourceNotFoundException("Incident", "incidentId", incidentId));
            
            incident.setAssignedHospitalId(hospitalId);
        	incident.setUpdatedAt(LocalDateTime.now());
        	incidentRepo.save(incident);

            // Decrement available beds
            hospital.setAvailableBeds(hospital.getAvailableBeds() - 1);

            // Update status if now at capacity
            if (hospital.getAvailableBeds() == 0) {
                hospital.setStatus(HospitalStatus.AT_CAPACITY);
            }

            hospital.setLastUpdated(LocalDateTime.now());
            hospitalRepo.save(hospital);

            // Invalidate capacity cache since it changed
            redisTemplate.delete("cache:hospital:capacity:" + hospitalId);

            // TODO: save an AuditLog entry here once AuditLog model is ready

            return "Hospital " + hospitalId + " successfully assigned to incident " + incidentId;

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Lock interrupted for hospital assignment");
        } finally {
            // Always release lock
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    @Override
    public Page<Hospital> getByStatus(HospitalStatus status, Pageable pageable) {
        return hospitalRepo.findByStatus(status, pageable);
    }
}
