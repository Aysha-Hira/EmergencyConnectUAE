//Section 104 - Group 4
//Aysha Hira(1088000), Fatima Syed(1095190), Laisa Isra(1089635)


package com.example.demo.hospitals.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import com.example.demo.hospitals.model.Hospital;
import com.example.demo.hospitals.model.HospitalStatus;


public interface HospitalRepository extends MongoRepository<Hospital, String> {
    Page<Hospital> findAll(Pageable pageable);
    Page<Hospital> findByStatus(HospitalStatus status, Pageable pageable);
}
