//Section 104 - Group 4
//Aysha Hira(1088000), Fatima Syed(1095190), Laisa Isra(1089635)


package com.example.demo.config;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/admin/ip")
@RequiredArgsConstructor
@PreAuthorize("hasRole('DISPATCHER')") // only admins/dispatchers
@Tag(name = "IP Management", description = "Blacklist and whitelist IP addresses")
public class IpManagementController {

    private final RedisTemplate<String, Object> redisTemplate;

    @PostMapping("/blacklist/{ip}")
    @Operation(summary = "Blacklist an IP address")
    public ResponseEntity<String> blacklist(@PathVariable String ip) {
        redisTemplate.opsForValue().set("blacklist:ip:" + ip, "manual-ban");
        return ResponseEntity.ok("IP blacklisted: " + ip);
    }

    @DeleteMapping("/blacklist/{ip}")
    @Operation(summary = "Remove IP from blacklist")
    public ResponseEntity<String> removeBlacklist(@PathVariable String ip) {
        redisTemplate.delete("blacklist:ip:" + ip);
        return ResponseEntity.ok("IP removed from blacklist: " + ip);
    }

    @PostMapping("/whitelist/{ip}")
    @Operation(summary = "Whitelist an IP address")
    public ResponseEntity<String> whitelist(@PathVariable String ip) {
        redisTemplate.opsForValue().set("whitelist:ip:" + ip, "trusted");
        return ResponseEntity.ok("IP whitelisted: " + ip);
    }

    @DeleteMapping("/whitelist/{ip}")
    @Operation(summary = "Remove IP from whitelist")
    public ResponseEntity<String> removeWhitelist(@PathVariable String ip) {
        redisTemplate.delete("whitelist:ip:" + ip);
        return ResponseEntity.ok("IP removed from whitelist: " + ip);
    }

    @GetMapping("/blacklist")
    @Operation(summary = "View all blacklisted IPs")
    public ResponseEntity<?> getBlacklist() {
        var keys = redisTemplate.keys("blacklist:ip:*");
        return ResponseEntity.ok(keys);
    }
}