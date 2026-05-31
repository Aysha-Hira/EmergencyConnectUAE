package com.example.demo.incidents.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import jakarta.validation.constraints.*;

import lombok.Data;

@Data
@Document(collection = "incidents")
public class Incident {

    @Id
    private String id;

    @Field(name = "title")
    @NotBlank(message = "Title is required")
    private String title;

    @Field(name = "description")
    @NotBlank(message = "Description is required")
    private String description;

    @Field(name = "type")
    @NotNull(message = "Type is required")
    private IncidentType type;

    @Field(name = "status")
    private IncidentStatus status;

    @Field(name = "severity")
    @NotNull(message = "Severity is required")
    private SeverityLevel severity;

    @Field(name = "location")
    @NotBlank(message = "Location is required")
    private String location;

    @Field(name = "latitude")
    private Double latitude;

    @Field(name = "longitude")
    private Double longitude;

    @Field(name = "reportedBy")
    private String reportedBy;

    @Field(name = "assignedDispatcher")
    private String assignedDispatcher;

    @Field(name = "assignedHospitalId")
    private String assignedHospitalId;

    @Field(name = "assignments")
    private List<Assignment> assignments = new ArrayList<>();

    @Field(name = "createdAt")
    private LocalDateTime createdAt;

    @Field(name = "updatedAt")
    private LocalDateTime updatedAt;

    @Field(name = "resolvedAt")
    private LocalDateTime resolvedAt;
}