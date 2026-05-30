package com.example.demo.emergency_unit.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.example.demo.emergency_unit.dto.UnitRequestDTO;
import com.example.demo.emergency_unit.model.EmergencyUnit;
import com.example.demo.emergency_unit.model.UnitStatus;

public interface EmergencyUnitService {

    Page<EmergencyUnit> getAllUnits(Pageable pageable);

    Page<EmergencyUnit> getAvailableUnits(Pageable pageable);

    EmergencyUnit getUnitById(String id);

    EmergencyUnit getUnitByName(String name);

    String getIdByName(String name);

    EmergencyUnit addNewUnit(UnitRequestDTO unitDTO);

    EmergencyUnit updateUnitStatus(String id, UnitStatus newStatus);

}
