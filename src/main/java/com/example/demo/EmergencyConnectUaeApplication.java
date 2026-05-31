package com.example.demo;

import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class EmergencyConnectUaeApplication {

	public static void main(String[] args) {
		SpringApplication.run(EmergencyConnectUaeApplication.class, args);
	}
	
	@Bean
	public ApplicationRunner clearStaleCache(CacheManager cacheManager) {
	    return args -> {
	        cacheManager.getCacheNames().forEach(name -> {
	            var cache = cacheManager.getCache(name);
	            if (cache != null) cache.clear();
	        });
	    };
	}

}
