package com.procgrid.productservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Test application that excludes problematic auto-configurations
 */
@SpringBootApplication(exclude = {
    org.springframework.cloud.netflix.eureka.EurekaClientAutoConfiguration.class
})
public class TestApplication {
    
    public static void main(String[] args) {
        System.setProperty("spring.cloud.config.enabled", "false");
        System.setProperty("spring.cloud.discovery.enabled", "false");
        System.setProperty("eureka.client.enabled", "false");
        
        SpringApplication app = new SpringApplication(TestApplication.class);
        app.setAdditionalProfiles("test");
        app.run(args);
    }
}