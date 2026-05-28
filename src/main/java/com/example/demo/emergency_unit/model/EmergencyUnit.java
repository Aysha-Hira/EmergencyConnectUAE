package com.example.demo.emergency_unit.model;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Data;

@Data
@Document(collection = "emergency_units")
public class EmergencyUnit {
    String id;

    @Indexed(unique = true)
    String unitName;

    UnitType unitType;

    Double latitude;

    Double longitude;

    UnitStatus status;

    String currentIncidentId;

    // depends on the unit type
    List<UnitResource> resources;

    String responderId;

    @CreatedDate
    LocalDateTime createdDate;

    @LastModifiedDate
    LocalDateTime updatedDate;

    // time when they got assigned to a incident
    // resets when status becomes available (?)
    LocalDateTime assignedAt;

}
