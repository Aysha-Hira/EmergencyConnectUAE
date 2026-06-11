//Section 104 - Group 4
//Aysha Hira(1088000), Fatima Syed(1095190), Laisa Isra(1089635)


package com.example.demo.emergency_unit.model;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Data;

@Data
@Document(collection = "emergency_units")
public class EmergencyUnit {
    @Id
    private String id;

    @Indexed(unique = true)
    private String unitName;

    private UnitType unitType;

    private Double latitude;

    private Double longitude;

    private UnitStatus status;

    private String currentIncidentId;

    @Indexed(unique = true)
    private String responderID;

    // depends on the unit type
    private List<UnitResource> resources;

    private String responderId;

    @CreatedDate
    private LocalDateTime createdDate;

    @LastModifiedDate
    private LocalDateTime updatedDate;

    // time when they got assigned to a incident
    // resets when status becomes available (?)
    private LocalDateTime assignedAt;
    
    private LocalDateTime reservationEndTime;
    private String reservationReason;
    private String reservationEmirates;

}
