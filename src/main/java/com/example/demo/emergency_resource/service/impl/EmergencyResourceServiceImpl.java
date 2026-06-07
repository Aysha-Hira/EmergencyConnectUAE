package com.example.demo.emergency_resource.service.impl;

import java.util.concurrent.TimeUnit;

import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.example.demo.emergency_resource.dto.ResourceRequestDTO;
import com.example.demo.emergency_resource.model.EmergencyResource;
import com.example.demo.emergency_resource.model.ResourceStatus;
import com.example.demo.emergency_resource.repository.EmergencyResourceRepository;
import com.example.demo.emergency_resource.service.EmergencyResourceService;
import com.example.demo.exceptions.ResourceNotFoundException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.example.demo.config.CachedPage;

@Service
@Slf4j
@RequiredArgsConstructor
public class EmergencyResourceServiceImpl implements EmergencyResourceService {

    private final EmergencyResourceRepository emergencyResourceRepository;
    private final RedissonClient redissonClient;

    @Override
    @Cacheable(value = "resources", key = "#pageable.pageNumber + '-' + #pageable.pageSize")
    public CachedPage<EmergencyResource> getAllResources(Pageable pageable) {
        log.info("CACHE MISS - hitting MongoDB for all resources, page {}", pageable.getPageNumber());
        Page<EmergencyResource> page = emergencyResourceRepository.findAll(pageable);
        return new CachedPage<>(page.getContent(), page.getNumber(), page.getSize(), page.getTotalElements(), page.getTotalPages());
    }

    @Override
    @Cacheable(value = "availableResources", key = "#pageable.pageNumber + '-' + #pageable.pageSize")
    public CachedPage<EmergencyResource> getAvailableResources(Pageable pageable) {
        log.info("CACHE MISS - hitting MongoDB for available resources, page {}", pageable.getPageNumber());
        Page<EmergencyResource> page = emergencyResourceRepository.findByStatus(ResourceStatus.AVAILABLE, pageable);
        return new CachedPage<>(page.getContent(), page.getNumber(), page.getSize(), page.getTotalElements(), page.getTotalPages());
    }

    @Override
    @Cacheable(value = "availableResourcesPerHospital", key = "#hospitalId + '-' + #pageable.pageNumber + '-' + #pageable.pageSize")
    public CachedPage<EmergencyResource> getAvailableResourcesPerHospital(Pageable pageable, String hospitalId) {
        log.info("CACHE MISS - hitting MongoDB for available resources for hospital {}, page {}", hospitalId, pageable.getPageNumber());
        Page<EmergencyResource> page = emergencyResourceRepository.findByStatusAndHospitalId(ResourceStatus.AVAILABLE, hospitalId, pageable);
        return new CachedPage<>(page.getContent(), page.getNumber(), page.getSize(), page.getTotalElements(), page.getTotalPages());
    }

    @Override
    @Caching(evict = {
            @CacheEvict(value = "resources", allEntries = true),
            @CacheEvict(value = "availableResources", allEntries = true),
            @CacheEvict(value = "availableResourcesPerHospital", allEntries = true)
    })
    public EmergencyResource addResource(ResourceRequestDTO resourceDTO) {
        EmergencyResource newResource = new EmergencyResource();
        newResource.setName(resourceDTO.getType());
        newResource.setAvailableQuantity(resourceDTO.getAvailableQuantity());
        newResource.setTotalQuantity(resourceDTO.getTotalQuantity());
        newResource.setHospitalId(resourceDTO.getHospitalId());

        if (resourceDTO.getAvailableQuantity() > resourceDTO.getTotalQuantity())
            throw new IllegalArgumentException("Available quantity cannot exceed total quantity");

        if (resourceDTO.getAvailableQuantity() < 0 || resourceDTO.getTotalQuantity() < 0) {
            throw new IllegalArgumentException("Quantities cannot be negative");
        }

        newResource.setStatus(getResourceStatus(resourceDTO.getAvailableQuantity(), resourceDTO.getTotalQuantity()));

        EmergencyResource saved = emergencyResourceRepository.save(newResource);
        return saved;

    }

    private ResourceStatus getResourceStatus(int available, int total) {
        if (available == 0) {
            return ResourceStatus.DEPLETED;
        } else if (available < total * 0.3) {
            return ResourceStatus.LOW;
        } else {
            return ResourceStatus.AVAILABLE;
        }
    }

    @Override
    @Caching(evict = {
            @CacheEvict(value = "resources", allEntries = true),
            @CacheEvict(value = "availableResources", allEntries = true),
            @CacheEvict(value = "availableResourcesPerHospital", allEntries = true)
    })
    public EmergencyResource updateResource(String resourceId, ResourceRequestDTO dto) {
        String lockKey = "lock:resource:update:" + resourceId;
        RLock lock = redissonClient.getLock(lockKey);

        try {
            boolean acquired = lock.tryLock(5, 10, TimeUnit.SECONDS);
            if (!acquired) {
                throw new RuntimeException("Resource is currently being updated, please try again");
            }

            EmergencyResource existing = emergencyResourceRepository.findById(resourceId)
                    .orElseThrow(() -> new ResourceNotFoundException("Resource", "resourceId", resourceId));

            existing.setName(dto.getType());
            existing.setAvailableQuantity(dto.getAvailableQuantity());
            existing.setTotalQuantity(dto.getTotalQuantity());
            existing.setHospitalId(dto.getHospitalId());
            existing.setStatus(getResourceStatus(dto.getAvailableQuantity(), dto.getTotalQuantity()));

            return emergencyResourceRepository.save(existing);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Lock interrupted during resource update");
        } finally {
            if (lock.isHeldByCurrentThread())
                lock.unlock();
        }
    }

    @Override
    @Caching(evict = {
            @CacheEvict(value = "resources", allEntries = true),
            @CacheEvict(value = "availableResources", allEntries = true),
            @CacheEvict(value = "availableResourcesPerHospital", allEntries = true)
    })
    public void deleteResource(String resourceId) {
        emergencyResourceRepository.deleteById(resourceId);
    }

}
