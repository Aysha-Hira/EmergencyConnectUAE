package com.example.demo.emergency_unit.dto;

import java.util.List;
import com.example.demo.emergency_unit.model.UnitResource;
import com.example.demo.emergency_unit.model.UnitType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UnitRequestDTO {

    @NotBlank(message = "Unit name is required")
    private String unitName;

    @NotNull(message = "Unit type is required")
    private UnitType unitType;

    private Double latitude;
    private Double longitude;

    private String responderId;

    // optional — not all units carry resources at creation time
    private List<UnitResource> resources;
}