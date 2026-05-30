package com.example.demo.emergency_resource.model;

import java.time.LocalDateTime;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import lombok.Data;

@Data
@Document(collection = "facility_resources")
public class EmergencyResource {
    @Id
    private String id;

    @Field("name")
    @Indexed
    private ResourceType name;

    @Field("availableQuantity")
    private int availableQuantity; // lock here?

    @Field("totalQuantity")
    private int totalQuantity;

    private ResourceStatus status; // AVAILABLE, LOW, DEPLETED

    @Field("hospitalId")
    private String hospitalId; // which hospital holds this

    @CreatedDate
    @Field("createdDate")
    private LocalDateTime createdDate;

    @LastModifiedDate
    @Field("updatedDate")
    private LocalDateTime updatedDate;
}
