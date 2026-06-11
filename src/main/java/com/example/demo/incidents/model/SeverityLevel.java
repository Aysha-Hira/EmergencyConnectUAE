//Section 104 - Group 4
//Aysha Hira(1088000), Fatima Syed(1095190), Laisa Isra(1089635)


package com.example.demo.incidents.model;

public enum SeverityLevel {
    LOW("Minor incident - can wait", "Respond within 30 minutes"),
    MEDIUM("Requires attention", "Respond within 15 minutes"),
    HIGH("Serious incident", "Respond within 10 minutes"),
    CRITICAL("Life-threatening", "Respond within 5 minutes - UAE Gold standard");
    
    private final String description;
    private final String uaeResponseTarget;
    
    SeverityLevel(String description, String uaeResponseTarget) {
        this.description = description;
        this.uaeResponseTarget = uaeResponseTarget;
    }
    
    public String getDescription() { return description; }
    public String getUaeResponseTarget() { return uaeResponseTarget; }
}

