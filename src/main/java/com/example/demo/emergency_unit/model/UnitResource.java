package com.example.demo.emergency_unit.model;

import lombok.Data;

@Data
public class UnitResource {
    private UnitResourceType type; // OXYGEN_TANK, DEFIBRILLATOR, etc.
    private int quantity;
    private String quantityMeasure;
}