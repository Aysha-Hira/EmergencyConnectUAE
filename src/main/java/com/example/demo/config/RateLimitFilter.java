package com.example.demo.config;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
public class RateLimitFilter implements Filter {
    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

    private final RedisTemplate<String, Object> redisTemplate;

    private static final String BLACKLIST_KEY = "blacklist:ip:";
    private static final String WHITELIST_KEY = "whitelist:ip:";

    // how many rate limit violations before auto-blacklisting
    private static final int MAX_VIOLATIONS = 5;
    private static final String VIOLATION_KEY = "violations:ip:";

    private Bucket createBucket() {
        Bandwidth limit = Bandwidth.classic(30, Refill.greedy(30, Duration.ofMinutes(1)));
        return Bucket.builder().addLimit(limit).build();
    }

    private Bucket getBucket(String ip) {
        return buckets.computeIfAbsent(ip, k -> createBucket());
    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;

        String path = request.getRequestURI();
        if (!path.startsWith("/api/")) {
            chain.doFilter(req, res);
            return;
        }

        String ip = request.getRemoteAddr();
        
        // check if ip is blacklisted - block immediately
        if (isBlacklisted(ip)) {
            response.setStatus(403);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\": \"Your IP has been blacklisted. Access Denied\"}");
            return;
        }

        // check if ip is whitelisted - skip rate limiting
        if (isWhitelisted(ip)) {
            chain.doFilter(req, res);
            return;
        }

        // for normal IPs, apply rate limiting
        Bucket bucket = getBucket(ip);

        if (bucket.tryConsume(1))
            chain.doFilter(req, res);

        else {
            recordViolation(ip);
            response.setStatus(429);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\": \"Too many requests — please slow down\"}");
        }
    }

    private boolean isBlacklisted(String ip) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(BLACKLIST_KEY + ip));
    }

    private boolean isWhitelisted(String ip) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(WHITELIST_KEY + ip));
    }

    private void recordViolation(String ip) {
        String key = VIOLATION_KEY + ip;
        Long violations = redisTemplate.opsForValue().increment(key);
        redisTemplate.expire(key, Duration.ofHours(1));

        if (violations != null && violations >= MAX_VIOLATIONS) {
            // auto-blacklist for 24 hours
            redisTemplate.opsForValue().set(BLACKLIST_KEY + ip, "auto-banned", Duration.ofHours(24));
        }
    }
}