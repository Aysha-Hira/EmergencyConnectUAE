package com.example.demo.audit.model;

import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "audit_logs")
public class AuditLog {
    
    @Id
    private String id;
    
    @Field("userId")
    @Indexed
    private String userId;          // Who performed the action
    
    @Field("username")
    @Indexed
    private String username;        // User's name (for easy querying)
    
    @Field("userRole")
    @Indexed
    private String userRole;        // DISPATCHER, RESPONDER, HOSPITAL_ADMIN
    
    @Field("action")
    @Indexed
    private String action;          // CREATE, UPDATE, DELETE, ASSIGN, STATUS_CHANGE, LOGIN, LOGOUT, MFA_ENABLE, MFA_DISABLE
    
    @Field("entityType")
    @Indexed
    private String entityType;      // INCIDENT, UNIT, RESOURCE, HOSPITAL, USER
    
    @Field("entityId")
    @Indexed
    private String entityId;        // ID of the affected entity
    
    @Field("oldValue")
    private String oldValue;        // JSON string of previous state (optional)
    
    @Field("newValue")
    private String newValue;        // JSON string of new state (optional)
    
    @Field("details")
    private String details;         // Human-readable description
    
    @Field("ipAddress")
    private String ipAddress;       // IP address of the requester
    
    @Field("timestamp")
    @Indexed
    private LocalDateTime timestamp;
    
    @Field("success")
    private boolean success;        // Was the operation successful?
    
    @Field("errorMessage")
    private String errorMessage;    // If failed, why?
}