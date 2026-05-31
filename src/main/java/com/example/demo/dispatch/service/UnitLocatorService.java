package com.example.demo.dispatch.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.example.demo.emergency_unit.model.EmergencyUnit;
import com.example.demo.emergency_unit.model.UnitStatus;
import com.example.demo.emergency_unit.repository.EmergencyUnitRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class UnitLocatorService {

    private final EmergencyUnitRepository unitRepository;
    
    // Earth's radius in kilometers
    private static final double EARTH_RADIUS_KM = 6371.0;
    
    /**
     * Calculate distance between two coordinates using Haversine formula
     */
    public double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                   Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                   Math.sin(dLon / 2) * Math.sin(dLon / 2);
        
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        
        return EARTH_RADIUS_KM * c;
    }
    
    /**
     * Find nearest available unit to incident location (any emirate)
     */
    public EmergencyUnit findNearestAvailableUnit(double incidentLat, double incidentLon) {
        List<EmergencyUnit> availableUnits = unitRepository.findAll().stream()
            .filter(unit -> unit.getStatus() == UnitStatus.AVAILABLE)
            .filter(unit -> unit.getLatitude() != null && unit.getLongitude() != null)
            .collect(Collectors.toList());
        
        if (availableUnits.isEmpty()) {
            log.warn("No available units found!");
            return null;
        }
        
        EmergencyUnit nearest = null;
        double minDistance = Double.MAX_VALUE;
        
        for (EmergencyUnit unit : availableUnits) {
            double distance = calculateDistance(
                incidentLat, incidentLon,
                unit.getLatitude(), unit.getLongitude()
            );
            
            if (distance < minDistance) {
                minDistance = distance;
                nearest = unit;
            }
        }
        
        if (nearest != null) {
            log.info("Nearest unit {} is {:.2f} km away", nearest.getUnitName(), minDistance);
        }
        
        return nearest;
    }
    
    /**
     * Find nearest available unit in the SAME EMIRATE (for realistic UAE dispatch)
     */
    public EmergencyUnit findNearestAvailableUnitInEmirate(double incidentLat, double incidentLon, String emirates) {
        // First try: Find units in the same emirate
        List<EmergencyUnit> sameEmirateUnits = unitRepository.findAll().stream()
            .filter(unit -> unit.getStatus() == UnitStatus.AVAILABLE)
            .filter(unit -> unit.getLatitude() != null && unit.getLongitude() != null)
            .filter(unit -> isUnitInEmirate(unit, emirates))
            .collect(Collectors.toList());
        
        if (!sameEmirateUnits.isEmpty()) {
            log.info("Found {} available units in {}", sameEmirateUnits.size(), emirates);
            return findNearestFromList(sameEmirateUnits, incidentLat, incidentLon);
        }
        
        // Fallback: Find nearest unit from neighboring emirates
        log.warn("No units available in {}, checking neighboring emirates...", emirates);
        List<EmergencyUnit> neighboringUnits = getUnitsInNeighboringEmirates(emirates);
        
        if (!neighboringUnits.isEmpty()) {
            log.info("Found {} available units in neighboring emirates", neighboringUnits.size());
            return findNearestFromList(neighboringUnits, incidentLat, incidentLon);
        }
        
        // Last resort: Any available unit
        log.warn("No units in neighboring emirates, searching all emirates...");
        return findNearestAvailableUnit(incidentLat, incidentLon);
    }
    
    /**
     * Find nearest unit from a list
     */
    private EmergencyUnit findNearestFromList(List<EmergencyUnit> units, double lat, double lon) {
        EmergencyUnit nearest = null;
        double minDistance = Double.MAX_VALUE;
        
        for (EmergencyUnit unit : units) {
            double distance = calculateDistance(lat, lon, unit.getLatitude(), unit.getLongitude());
            if (distance < minDistance) {
                minDistance = distance;
                nearest = unit;
            }
        }
        
        if (nearest != null) {
            log.info("Nearest unit {} is {:.2f} km away", nearest.getUnitName(), minDistance);
        }
        
        return nearest;
    }
    
    /**
     * Determine which emirate a unit belongs to based on coordinates
     */
    private boolean isUnitInEmirate(EmergencyUnit unit, String emirates) {
        double lat = unit.getLatitude();
        double lon = unit.getLongitude();
        
        switch (emirates.toLowerCase()) {
            case "dubai":
                return lat >= 24.95 && lat <= 25.35 && lon >= 55.15 && lon <= 55.45;
            case "abu dhabi":
                return lat >= 22.5 && lat <= 25.0 && lon >= 51.0 && lon <= 55.0;
            case "sharjah":
                return lat >= 25.25 && lat <= 25.45 && lon >= 55.35 && lon <= 55.55;
            case "ajman":
                return lat >= 25.40 && lat <= 25.43 && lon >= 55.43 && lon <= 55.45;
            case "ras al khaimah":
                return lat >= 25.5 && lat <= 26.0 && lon >= 55.8 && lon <= 56.1;
            case "fujairah":
                return lat >= 25.0 && lat <= 25.5 && lon >= 56.2 && lon <= 56.4;
            case "umm al quwain":
                return lat >= 25.5 && lat <= 25.6 && lon >= 55.5 && lon <= 55.6;
            default:
                return true;
        }
    }
    
    /**
     * Get units from neighboring emirates for cross-emirate dispatch
     */
    private List<EmergencyUnit> getUnitsInNeighboringEmirates(String emirates) {
        List<String> neighboringEmirates;
        
        switch (emirates.toLowerCase()) {
            case "dubai":
                neighboringEmirates = List.of("sharjah", "abu dhabi");
                break;
            case "abu dhabi":
                neighboringEmirates = List.of("dubai");
                break;
            case "sharjah":
                neighboringEmirates = List.of("dubai", "ajman", "umm al quwain");
                break;
            case "ajman":
                neighboringEmirates = List.of("sharjah", "umm al quwain");
                break;
            case "ras al khaimah":
                neighboringEmirates = List.of("umm al quwain", "fujairah");
                break;
            case "fujairah":
                neighboringEmirates = List.of("sharjah", "ras al khaimah");
                break;
            default:
                neighboringEmirates = List.of();
        }
        
        return unitRepository.findAll().stream()
            .filter(unit -> unit.getStatus() == UnitStatus.AVAILABLE)
            .filter(unit -> unit.getLatitude() != null && unit.getLongitude() != null)
            .filter(unit -> {
                for (String neighbor : neighboringEmirates) {
                    if (isUnitInEmirate(unit, neighbor)) {
                        return true;
                    }
                }
                return false;
            })
            .collect(Collectors.toList());
    }
    
    /**
     * Get response time estimate based on distance and emirate
     */
    public String getEstimatedResponseTime(double distanceKm, String emirates) {
        double estimatedMinutes;
        
        if (emirates.equalsIgnoreCase("dubai") || emirates.equalsIgnoreCase("abu dhabi")) {
            estimatedMinutes = (distanceKm / 40) * 60;
        } else {
            estimatedMinutes = (distanceKm / 60) * 60;
        }
        
        if (estimatedMinutes < 5) {
            return "Under 5 minutes (UAE Gold standard)";
        } else if (estimatedMinutes < 10) {
            return String.format("Approximately %.0f minutes", estimatedMinutes);
        } else if (estimatedMinutes < 20) {
            return String.format("Approximately %.0f-%.0f minutes", estimatedMinutes, estimatedMinutes + 5);
        } else {
            return String.format("Approximately %.0f minutes (consider closer unit)", estimatedMinutes);
        }
    }
}