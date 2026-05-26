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