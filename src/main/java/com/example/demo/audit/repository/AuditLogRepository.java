package com.example.demo.audit.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import com.example.demo.audit.model.AuditLog;

public interface AuditLogRepository extends MongoRepository<AuditLog, String> {
    
    // Get logs by user
    Page<AuditLog> findByUserId(String userId, Pageable pageable);
    
    // Get logs by entity (incident, unit, etc.)
    Page<AuditLog> findByEntityTypeAndEntityId(String entityType, String entityId, Pageable pageable);
    
    // Get logs by action type
    Page<AuditLog> findByAction(String action, Pageable pageable);
    
    // Get logs by date range
    Page<AuditLog> findByTimestampBetween(LocalDateTime start, LocalDateTime end, Pageable pageable);
    
    // Get recent logs for an entity
    List<AuditLog> findTop10ByEntityTypeAndEntityIdOrderByTimestampDesc(String entityType, String entityId);
    
    // Search by username or action (custom query)
    @Query("{ $or: [ { 'username': { $regex: ?0, $options: 'i' } }, { 'action': { $regex: ?0, $options: 'i' } }, { 'details': { $regex: ?0, $options: 'i' } } ] }")
    Page<AuditLog> search(String keyword, Pageable pageable);
}