package com.example.demo.emergency_resource.model;

import java.time.LocalDateTime;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import lombok.Data;

@Data
@Document(collection = "facility_resources")
public class EmergencyResource {
    @Id
    private String id;

    // private ResourceCategory resourceCategory;
    @Field("resourceName")
    @Indexed
    private FacilityResource resourceName;

    @Field("availableQuantity")
    private int availableQuantity; // lock here?

    @Field("totalQuantity")
    private int totalQuantity;

    @Field("quantityMeasure")
    private String quantityMeasure; // e.g. "units", "liters", "beds"

    @Field("hospitalId")
    private String hospitalId; // which hospital/unit holds this

    @Field("isLow")
    private boolean isLow; // flag for scarce resources

    @CreatedDate
    @Field("createdDate")
    private LocalDateTime createdDate;

    @LastModifiedDate
    @Field("updatedDate")
    private LocalDateTime updatedDate;

    @Version
    private Long version; // for concurrency
}
