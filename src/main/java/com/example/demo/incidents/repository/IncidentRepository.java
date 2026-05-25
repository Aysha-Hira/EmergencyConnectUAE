package com.example.demo.incidents.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import com.example.demo.incidents.model.Incident;

public interface IncidentRepository extends MongoRepository<Incident, String>{

}
