//Section 104 - Group 4
//Aysha Hira(1088000), Fatima Syed(1095190), Laisa Isra(1089635)


package com.example.demo.emergency_unit.dto;

import java.time.LocalDateTime;

public class ReservationRequestDTO {
    private String unitId;
    private String reason;
    private String emirates;
    private LocalDateTime endTime;

    public String getUnitId() { return unitId; }
    public void setUnitId(String unitId) { this.unitId = unitId; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public String getEmirates() { return emirates; }
    public void setEmirates(String emirates) { this.emirates = emirates; }
    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }
}