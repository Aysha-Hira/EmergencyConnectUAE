package com.example.demo.incidents.dto;

import lombok.Data;

@Data
public class AssignmentRequestDTO {
    private String unitId;
    private String unitType;
    private String assignedBy; // TODO: assignedBy should come from JWT principal, not request body
    private String notes;
}