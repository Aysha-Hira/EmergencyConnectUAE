package com.example.demo.emergency_unit.service.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.example.demo.config.CachedPage;
import com.example.demo.emergency_unit.dto.ReservationRequestDTO;
import com.example.demo.emergency_unit.dto.UnitRequestDTO;
import com.example.demo.emergency_unit.model.EmergencyUnit;
import com.example.demo.emergency_unit.model.UnitStatus;
import com.example.demo.emergency_unit.repository.EmergencyUnitRepository;
import com.example.demo.emergency_unit.service.EmergencyUnitService;
import com.example.demo.exceptions.ResourceNotFoundException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class EmergencyUnitServiceImpl implements EmergencyUnitService {

    private final EmergencyUnitRepository emergencyUnitRepository;
    // private final RedissonClient redissonClient;

    @Override
    @Cacheable(value = "units", key = "#pageable.pageNumber + '-' + #pageable.pageSize")
    public CachedPage<EmergencyUnit> getAllUnits(Pageable pageable) {
        log.info("CACHE MISS - hitting MongoDB for all units, page {}", pageable.getPageNumber());
        Page<EmergencyUnit> page = emergencyUnitRepository.findAll(pageable);
        return new CachedPage<>(page.getContent(), page.getNumber(), page.getSize(), page.getTotalElements(), page.getTotalPages());
    }

    @Override
    @Cacheable(value = "units", key = "'available-' + #pageable.pageNumber + '-' + #pageable.pageSize")
    public CachedPage<EmergencyUnit> getAvailableUnits(Pageable pageable) {
        log.info("CACHE MISS - hitting MongoDB for available units, page {}", pageable.getPageNumber());
        Page<EmergencyUnit> page = emergencyUnitRepository.findByStatusIn(List.of(UnitStatus.AVAILABLE), pageable);
        return new CachedPage<>(page.getContent(), page.getNumber(), page.getSize(), page.getTotalElements(), page.getTotalPages());
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

    // @Override
    public String getIdByResponderId(String responderId) {
        EmergencyUnit emergencyUnit = emergencyUnitRepository.findByResponderID(responderId)
                .orElseThrow(() -> new ResourceNotFoundException("Unit", "ResponderId", responderId));
        return emergencyUnit.getId();
    }

    @Override
    @CacheEvict(value = "units", allEntries = true)
    public EmergencyUnit addNewUnit(UnitRequestDTO unitDTO) {
        if (emergencyUnitRepository.existsByUnitName(unitDTO.getUnitName())) {
            throw new IllegalArgumentException("Unit name already exists: " + unitDTO.getUnitName());
        }
        EmergencyUnit newUnit = new EmergencyUnit();
        newUnit.setUnitName(unitDTO.getUnitName());
        newUnit.setUnitType(unitDTO.getUnitType());
        newUnit.setStatus(UnitStatus.AVAILABLE);
        newUnit.setLatitude(unitDTO.getLatitude());
        newUnit.setLongitude(unitDTO.getLongitude());
        newUnit.setResponderID(unitDTO.getResponderId());
        newUnit.setResources(unitDTO.getResources());

        return emergencyUnitRepository.save(newUnit);
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
        } else if (newStatus == UnitStatus.DISPATCHED) {
            unit.setAssignedAt(LocalDateTime.now());
        }

        return emergencyUnitRepository.save(unit);
    }

    @Override
    @CacheEvict(value = "units", allEntries = true)
    public EmergencyUnit reserveUnit(ReservationRequestDTO dto) {
        EmergencyUnit unit = emergencyUnitRepository.findById(dto.getUnitId())
                .orElseThrow(() -> new ResourceNotFoundException("Unit", "unitId", dto.getUnitId()));

        if (unit.getStatus() != UnitStatus.AVAILABLE) {
            throw new RuntimeException("Unit " + dto.getUnitId() + " is not available for reservation");
        }

        if (dto.getEndTime().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("End time must be in the future");
        }

        unit.setStatus(UnitStatus.RESERVED);
        unit.setReservationEndTime(dto.getEndTime());
        unit.setReservationReason(dto.getReason());
        unit.setReservationEmirates(dto.getEmirates());

        log.info("Unit {} reserved until {}", dto.getUnitId(), dto.getEndTime());
        return emergencyUnitRepository.save(unit);
    }

    @Override
    @CacheEvict(value = "units", allEntries = true)
    public EmergencyUnit cancelReservation(String unitId) {
        EmergencyUnit unit = emergencyUnitRepository.findById(unitId)
                .orElseThrow(() -> new ResourceNotFoundException("Unit", "unitId", unitId));

        if (unit.getStatus() != UnitStatus.RESERVED) {
            throw new RuntimeException("Unit " + unitId + " is not currently reserved");
        }

        unit.setStatus(UnitStatus.AVAILABLE);
        unit.setReservationEndTime(null);
        unit.setReservationReason(null);
        unit.setReservationEmirates(null);

        log.info("Reservation cancelled for unit {}", unitId);
        return emergencyUnitRepository.save(unit);
    }

    @Override
    @CacheEvict(value = "units", allEntries = true)
    public void expireReservations() {
        List<EmergencyUnit> expiredUnits = emergencyUnitRepository
                .findByStatusIn(List.of(UnitStatus.RESERVED))
                .stream()
                .filter(unit -> unit.getReservationEndTime() != null
                        && unit.getReservationEndTime().isBefore(LocalDateTime.now()))
                .collect(Collectors.toList());

        for (EmergencyUnit unit : expiredUnits) {
            unit.setStatus(UnitStatus.AVAILABLE);
            unit.setReservationEndTime(null);
            unit.setReservationReason(null);
            unit.setReservationEmirates(null);
            emergencyUnitRepository.save(unit);
            log.info("Unit {} reservation expired, released to AVAILABLE", unit.getId());
        }
    }

}
