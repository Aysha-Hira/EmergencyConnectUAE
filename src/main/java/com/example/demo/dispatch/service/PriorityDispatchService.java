package com.example.demo.dispatch.service;

import java.util.Set;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.example.demo.incidents.model.Incident;
import com.example.demo.incidents.model.SeverityLevel;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class PriorityDispatchService {

    private final RedisTemplate<String, Object> redisTemplate;
    
    // Redis key for the priority queue
    private static final String PRIORITY_QUEUE_KEY = "dispatch:priority:queue";
    
    // Score mapping for severity levels (higher score = higher priority)
    private static final int CRITICAL_SCORE = 100;
    private static final int HIGH_SCORE = 75;
    private static final int MEDIUM_SCORE = 50;
    private static final int LOW_SCORE = 25;
    
    /**
     * Get priority score for a severity level
     */
    public int getPriorityScore(SeverityLevel severity) {
        switch (severity) {
            case CRITICAL:
                return CRITICAL_SCORE;
            case HIGH:
                return HIGH_SCORE;
            case MEDIUM:
                return MEDIUM_SCORE;
            case LOW:
                return LOW_SCORE;
            default:
                return 0;
        }
    }
    
    /**
     * Add an incident to the priority queue
     */
    public void addToQueue(Incident incident) {
        int score = getPriorityScore(incident.getSeverity());
        String queueItem = incident.getId() + ":" + incident.getSeverity().name();
        
        redisTemplate.opsForZSet().add(PRIORITY_QUEUE_KEY, queueItem, score);
        log.info("Added incident {} with priority {} to dispatch queue", 
                 incident.getId(), incident.getSeverity());
    }
    
    /**
     * Get the highest priority incident from queue
     * Returns: incidentId, or null if queue empty
     */
    public String getHighestPriorityIncident() {
        Set<Object> topItem = redisTemplate.opsForZSet()
            .reverseRangeByScore(PRIORITY_QUEUE_KEY, 0, 100, 0, 1);
        
        if (topItem == null || topItem.isEmpty()) {
            return null;
        }
        
        String item = (String) topItem.iterator().next();
        String incidentId = item.split(":")[0];
        
        // Remove from queue
        redisTemplate.opsForZSet().remove(PRIORITY_QUEUE_KEY, item);
        
        return incidentId;
    }
    
    /**
     * Get queue size
     */
    public Long getQueueSize() {
        return redisTemplate.opsForZSet().size(PRIORITY_QUEUE_KEY);
    }
    
    /**
     * Get all incidents in queue (for debugging)
     */
    public Set<Object> getAllInQueue() {
        return redisTemplate.opsForZSet()
            .reverseRangeByScore(PRIORITY_QUEUE_KEY, 0, 100);
    }
    
    /**
     * Remove incident from queue (if cancelled/resolved)
     */
    public void removeFromQueue(String incidentId) {
        Set<Object> allItems = redisTemplate.opsForZSet()
            .reverseRangeByScore(PRIORITY_QUEUE_KEY, 0, 100);
        
        if (allItems != null) {
            for (Object item : allItems) {
                String itemStr = (String) item;
                if (itemStr.startsWith(incidentId + ":")) {
                    redisTemplate.opsForZSet().remove(PRIORITY_QUEUE_KEY, item);
                    break;
                }
            }
        }
    }
}