//Section 104 - Group 4
//Aysha Hira(1088000), Fatima Syed(1095190), Laisa Isra(1089635)


package com.example.demo.emergency_resource.dto;

import com.example.demo.emergency_resource.model.ResourceType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ResourceRequestDTO {

    @NotNull(message = "Resource type is required")
    private ResourceType type;

    @Min(value = 0, message = "Available quantity cannot be negative")
    private int availableQuantity;

    @Min(value = 1, message = "Total quantity must be at least 1")
    private int totalQuantity;

    @NotBlank(message = "Hospital ID is required")
    private String hospitalId;
}