package com.example.demo.hospitals.model;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.*;

import com.example.demo.hospitals.model.HospitalStatus;

import lombok.Data;

@Data
@Document(collection = "hospitals")
public class Hospital {
	@Id
    private String id;
	
	@Field(name="name")
    private String name;
	
	@Field(name="address")
    private String address;
	
	@Field(name="latitude")
    private Double latitude;
	
	@Field(name="longitude")
    private Double longitude;
	
	@Field(name="totalBeds")
    private Integer totalBeds;
	
	@Field(name="availableBeds")
    private Integer availableBeds;
	
	@Field(name="icuBeds")
    private Integer icuBeds;
	
	@Field(name="availableIcuBeds")
    private Integer availableIcuBeds;
	
	@Field(name="erOperational")
    private Boolean erOperational;
	
	@Field(name="resourceIds")
    private List<String> resourceIds; //TODO: make this a list of resources
	
	@Field(name="status")
    private HospitalStatus status;
	
	@Field(name="lastUpdated")
    private LocalDateTime lastUpdated;
}