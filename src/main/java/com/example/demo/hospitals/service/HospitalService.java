//Section 104 - Group 4
//Aysha Hira(1088000), Fatima Syed(1095190), Laisa Isra(1089635)


package com.example.demo.hospitals.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.example.demo.config.CachedPage;
import com.example.demo.hospitals.dto.HospitalCapacityDTO;
import com.example.demo.hospitals.model.Hospital;
import com.example.demo.hospitals.model.HospitalStatus;

public interface HospitalService {
	CachedPage<Hospital> getAllHospitals(Pageable pageable);
	// CRUD
	Hospital saveNew(Hospital h);

	Hospital getById(String id);

	Hospital update(Hospital h, String id);

	void delete(String id);

	// Custom
	HospitalCapacityDTO getCapacity(String id);

	String assign(String hospitalId, String incidentId);

	CachedPage<Hospital> getByStatus(HospitalStatus status, Pageable pageable);
}
