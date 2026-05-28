package com.example.demo.emergency_unit.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import com.example.demo.emergency_unit.model.EmergencyUnit;
import com.example.demo.emergency_unit.model.UnitStatus;

public interface EmergencyUnitRepository extends MongoRepository<EmergencyUnit, String> {

    Page<EmergencyUnit> findByStatusIn(List<UnitStatus> units, Pageable pageable);

    Optional<EmergencyUnit> findByUnitName(String name);

}
