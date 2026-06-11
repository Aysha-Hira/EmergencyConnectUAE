//Section 104 - Group 4
//Aysha Hira(1088000), Fatima Syed(1095190), Laisa Isra(1089635)


package com.example.demo.hospitals.dto;

import com.example.demo.hospitals.model.HospitalStatus;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
@NoArgsConstructor 
public class HospitalCapacityDTO {
    private String hospitalId;
    private String name;
    private Integer totalBeds;
    private Integer availableBeds;
    private Integer icuBeds;
    private Integer availableIcuBeds;
    private Boolean erOperational;
    private HospitalStatus status;
}