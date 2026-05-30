package com.example.demo.emergency_resource.dto;

import java.time.LocalDateTime;
import com.example.demo.emergency_resource.model.ResourceStatus;
import com.example.demo.emergency_resource.model.ResourceType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ResourceResponseDTO {
    private String id;
    private ResourceType name;
    private int availableQuantity;
    private int totalQuantity;
    private ResourceStatus status;
    private String hospitalId;
    private LocalDateTime createdDate;
    private LocalDateTime updatedDate;
}