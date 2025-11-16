package com.procgrid.productservice;

import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * Product Service Application
 * Main entry point for the Product microservice in the B2B e-commerce platform
 */
@Slf4j
@SpringBootApplication
@EnableDiscoveryClient
@EnableCaching
@EnableAsync
@EnableScheduling
@EnableTransactionManagement
@MapperScan("com.procgrid.productservice.mapper")
public class ProductServiceApplication {
    
    public static void main(String[] args) {
        try {
            log.info("Starting Product Service Application...");
            
            SpringApplication.run(ProductServiceApplication.class, args);
            log.info("Product Service Application started successfully!");
        } catch (Exception e) {
            log.error("Failed to start Product Service Application", e);
            System.exit(1);
        }
    }
}