//Section 104 - Group 4
//Aysha Hira(1088000), Fatima Syed(1095190), Laisa Isra(1089635)


package com.example.demo.incidents.model;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class Assignment {
    private String id; // we set this manually as a UUID
    private String unitId; // which emergency unit was assigned
    private String unitType; // snapshot of unit type at time of assignment
    private String assignedBy; // userId of dispatcher
    private AssignmentStatus status;
    private LocalDateTime assignedAt;
    private LocalDateTime completedAt;
    private String notes;
}