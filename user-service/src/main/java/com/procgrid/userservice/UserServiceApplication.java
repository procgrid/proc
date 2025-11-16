package com.procgrid.userservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * Main application class for User Service
 */
@SpringBootApplication(scanBasePackages = {
    "com.procgrid.userservice",
    "com.procgrid.common"
})
@EnableDiscoveryClient
@EnableCaching
@EnableTransactionManagement
public class UserServiceApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(UserServiceApplication.class, args);
    }
}