package com.example.demo.emergency_unit.service.impl;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.example.demo.emergency_unit.model.EmergencyUnit;
import com.example.demo.emergency_unit.model.UnitStatus;
import com.example.demo.emergency_unit.repository.EmergencyUnitRepository;
import com.example.demo.emergency_unit.service.EmergencyUnitService;
import com.example.demo.exceptions.ResourceNotFoundException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EmergencyUnitServiceImpl implements EmergencyUnitService {

    private final EmergencyUnitRepository emergencyUnitRepository;
    // private final RedissonClient redissonClient;

    @Override
    public Page<EmergencyUnit> getAllUnits(Pageable pageable) {
        return emergencyUnitRepository.findAll(pageable);
    }

    @Override
    public Page<EmergencyUnit> getAvailableUnits(Pageable pageable) {
        return emergencyUnitRepository.findByStatusIn(
                List.of(UnitStatus.AVAILABLE, UnitStatus.RETURNING), pageable);
    }

    @Override
    public EmergencyUnit getUnitById(String id) {
        return emergencyUnitRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Unit", "UnitId", id));
    }

    @Override
    public EmergencyUnit getUnitByName(String name) {
        return emergencyUnitRepository.findByUnitName(name)
                .orElseThrow(() -> new ResourceNotFoundException("Unit", "UnitName", name));

    }

    @Override
    public String getIdByName(String name) {
        EmergencyUnit emergencyUnit = emergencyUnitRepository.findByUnitName(name)
                .orElseThrow(() -> new ResourceNotFoundException("Unit", "UnitName", name));
        return emergencyUnit.getId();
    }

    @Override
    @CacheEvict(value = "units", allEntries = true)
    public EmergencyUnit updateUnitStatus(String id, UnitStatus newStatus) {
        EmergencyUnit unit = emergencyUnitRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Unit", "UnitId", id));

        unit.setStatus(newStatus);

        if (newStatus == UnitStatus.AVAILABLE) {
            unit.setAssignedAt(null);
            unit.setStatus(newStatus);
        } else if (newStatus == UnitStatus.RESPONDING_TO_INCIDENT) {
            unit.setAssignedAt(LocalDateTime.now());
        }

        return emergencyUnitRepository.save(unit);
    }

    //

}
