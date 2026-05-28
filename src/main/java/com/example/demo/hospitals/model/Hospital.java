package com.example.demo.hospitals.model;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Document(collection = "hospitals")
@Data
public class Hospital {
	@Id
	private String id;

	@Field(name = "name")
	@NotBlank(message = "Name is required")
	private String name;

	@Field(name = "address")
	@NotBlank(message = "Address is required")
	private String address;

	@Field(name = "latitude")
	private Double latitude;

	@Field(name = "longitude")
	private Double longitude;

	@Field(name = "totalBeds")
	@NotNull(message = "Total beds is required")
	@Min(value = 1, message = "Must have at least 1 bed")
	private Integer totalBeds;

	@Field(name = "availableBeds")
	private Integer availableBeds;

	@Field(name = "icuBeds")
	private Integer icuBeds;

	@Field(name = "availableIcuBeds")
	private Integer availableIcuBeds;

	@Field(name = "erOperational")
	private Boolean erOperational;

	@Field(name = "resourceIds")
	private List<String> resourceIds; // TODO: make this a list of resources

	@Field(name = "status")
	private HospitalStatus status;

	@Field(name = "lastUpdated")
	private LocalDateTime lastUpdated;
}