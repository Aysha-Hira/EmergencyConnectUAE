//Section 104 - Group 4
//Aysha Hira(1088000), Fatima Syed(1095190), Laisa Isra(1089635)

package com.example.demo.uae;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/uae")
@Tag(name = "UAE Emergency Services", description = "UAE-specific emergency workflows")
public class UaeEmergencyController {

    @Value("${uae.emirates.list}")
    private List<String> emirates;

    @Value("${uae.emergency.ambulance}")
    private String ambulanceNumber;

    @Value("${uae.emergency.police}")
    private String policeNumber;

    @Value("${uae.emergency.civil_defense}")
    private String civilDefenseNumber;

    @GetMapping("/emergency-numbers")
    @Operation(summary = "Get UAE emergency contact numbers")
    public ResponseEntity<Map<String, String>> getEmergencyNumbers() {
        return ResponseEntity.ok(Map.of(
            "ambulance", ambulanceNumber,
            "police", policeNumber,
            "civil_defense", civilDefenseNumber,
            "coast_guard", "996",
            "electricity_emergency", "991",
            "water_emergency", "992"
        ));
    }

    @GetMapping("/emirates")
    @Operation(summary = "Get list of all UAE emirates")
    public ResponseEntity<List<String>> getEmirates() {
        return ResponseEntity.ok(emirates);
    }

    @GetMapping("/landmarks")
    @Operation(summary = "Get common UAE landmarks for incident reporting")
    public ResponseEntity<List<Map<String, String>>> getLandmarks() {
        List<Map<String, String>> landmarks = List.of(
            Map.of("name", "Burj Khalifa", "emirate", "Dubai", "lat", "25.1972", "lng", "55.2744"),
            Map.of("name", "Sheikh Zayed Grand Mosque", "emirate", "Abu Dhabi", "lat", "24.4126", "lng", "54.4745"),
            Map.of("name", "Dubai Mall", "emirate", "Dubai", "lat", "25.1982", "lng", "55.2797"),
            Map.of("name", "Palm Jumeirah", "emirate", "Dubai", "lat", "25.1095", "lng", "55.1567"),
            Map.of("name", "Expo City Dubai", "emirate", "Dubai", "lat", "24.9577", "lng", "55.1443"),
            Map.of("name", "Yas Island", "emirate", "Abu Dhabi", "lat", "24.4838", "lng", "54.6068"),
            Map.of("name", "Sharjah Corniche", "emirate", "Sharjah", "lat", "25.3577", "lng", "55.3983"),
            Map.of("name", "Ajman Beach", "emirate", "Ajman", "lat", "25.4164", "lng", "55.4399"),
            Map.of("name", "RAK City", "emirate", "Ras Al Khaimah", "lat", "25.7895", "lng", "55.9432")
        );
        return ResponseEntity.ok(landmarks);
    }

    @GetMapping("/emergency-protocols")
@Operation(summary = "Get UAE emergency response protocols")
@PreAuthorize("hasRole('DISPATCHER')")
public ResponseEntity<Map<String, Object>> getEmergencyProtocols() {
    return ResponseEntity.ok(Map.of(
        "critical_incidents", List.of(
            "Structure fire in high-rise building (Dubai Marina, Downtown)",
            "Terrorist attack or security threat",
            "Major traffic accident on Sheikh Zayed Road",
            "Medical emergency involving VIP or royal family",
            "Industrial fire in Abu Dhabi's industrial zones"
        ),
        "response_times_by_emirate", Map.of(
            "dubai", Map.of(
                "urban_core", "8 minutes (Downtown, Marina, Deira)",
                "suburban", "12 minutes (Al Qusais, Al Nahda)",
                "remote_areas", "20 minutes (Hatta, Lehbab)"
            ),
            "abu_dhabi", Map.of(
                "city_center", "10 minutes (Corniche, Al Reem Island)",
                "suburban", "15 minutes (Khalifa City, Mohammed Bin Zayed City)",
                "western_region", "30 minutes (Madinat Zayed, Ruwais)"
            ),
            "sharjah", Map.of(
                "urban", "10 minutes (Al Qasimia, Al Majaz)",
                "suburban", "15 minutes (Al Rahmaniya, Al Tay)"
            ),
            "ras_al_khaimah", Map.of(
                "city_areas", "12 minutes",
                "mountain_areas", "25 minutes (Jebel Jais)"
            )
        ),
        "cross_emirate_protocol", Map.of(
            "dubai_to_sharjah", "Initial response from within Sharjah, Dubai units support only if requested",
            "dubai_to_abu_dhabi", "No - Abu Dhabi units handle Abu Dhabi incidents",
            "principle", "Units dispatched from SAME emirate to meet response time targets",
            "exception", "Major national emergencies (e.g., Expo, National Day) - coordinated by Ministry of Interior"
        ),
        "special_considerations", List.of(
            "Friday prayers (12:00-14:00) - increased traffic near mosques across all emirates",
            "Public holidays - coordination with Ministry of Interior",
            "Ramadan - adjusted shift schedules, slower traffic around iftar time (18:00-19:30)",
            "Expo/Events - dedicated emergency teams on site",
            "Jebel Jais (RAK) - specialized mountain rescue teams required",
            "Desert areas - 4x4 response units needed"
        )
    ));
}

    @GetMapping("/hospitals")
    @Operation(summary = "Get major UAE hospitals for emergency dispatch")
    public ResponseEntity<List<Map<String, String>>> getMajorHospitals() {
        List<Map<String, String>> hospitals = List.of(
            Map.of("name", "Rashid Hospital", "emirate", "Dubai", "lat", "25.2687", "lng", "55.3098"),
            Map.of("name", "Dubai Hospital", "emirate", "Dubai", "lat", "25.2668", "lng", "55.3164"),
            Map.of("name", "Cleveland Clinic Abu Dhabi", "emirate", "Abu Dhabi", "lat", "24.4482", "lng", "54.4353"),
            Map.of("name", "Sheikh Khalifa Medical City", "emirate", "Abu Dhabi", "lat", "24.4667", "lng", "54.3667"),
            Map.of("name", "Kuwait Hospital", "emirate", "Sharjah", "lat", "25.3577", "lng", "55.3918")
        );
        return ResponseEntity.ok(hospitals);
    }
}