package com.example.demo.emergency_unit.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.example.demo.config.CachedPage;
import com.example.demo.emergency_unit.dto.ReservationRequestDTO;
import com.example.demo.emergency_unit.dto.UnitRequestDTO;
import com.example.demo.emergency_unit.model.EmergencyUnit;
import com.example.demo.emergency_unit.model.UnitStatus;

public interface EmergencyUnitService {

	CachedPage<EmergencyUnit> getAllUnits(Pageable pageable);

	CachedPage<EmergencyUnit> getAvailableUnits(Pageable pageable);

    EmergencyUnit getUnitById(String id);

    EmergencyUnit getUnitByName(String name);

    String getIdByName(String name);

    EmergencyUnit addNewUnit(UnitRequestDTO unitDTO);

    EmergencyUnit updateUnitStatus(String id, UnitStatus newStatus);
    
    EmergencyUnit reserveUnit(ReservationRequestDTO dto);
    EmergencyUnit cancelReservation(String unitId);
    void expireReservations();

}
