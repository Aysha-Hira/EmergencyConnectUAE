package com.example.demo.emergency_unit.model;

//TODO: change to just available, responding_to_incident, unavailable
public enum UnitStatus {
    AVAILABLE,
    RESPONDING_TO_INCIDENT,
    REACHED_INCIDENT_LOCATION,
    RETURNING,
    UNDER_MAINTENANCE,
    RESTOCKING
}
