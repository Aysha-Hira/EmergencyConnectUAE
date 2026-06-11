//Section 104 - Group 4
//Aysha Hira(1088000), Fatima Syed(1095190), Laisa Isra(1089635)


package com.example.demo.emergency_unit.model;

import lombok.Data;

@Data
public class UnitResource {
    private UnitResourceType type; // OXYGEN_TANK, DEFIBRILLATOR, etc.
    private int quantity;
    private String quantityMeasure;
}