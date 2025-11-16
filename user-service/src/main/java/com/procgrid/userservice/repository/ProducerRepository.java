package com.procgrid.userservice.repository;

import com.procgrid.userservice.mapper.mybatis.ProducerMyBatisMapper;
import com.procgrid.userservice.model.Producer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository layer for Producer entity
 */
@Repository
@RequiredArgsConstructor
@Slf4j
public class ProducerRepository {
    
    private final ProducerMyBatisMapper producerMapper;
    
    /**
     * Save producer
     */
    @CacheEvict(value = {"producers", "producerStats"}, allEntries = true)
    public Producer save(Producer producer) {
        log.debug("Saving producer: {}", producer.getFarmName());
        
        if (producer.getId() == null) {
            producer.setId(UUID.randomUUID().toString());
            producer.setCreatedAt(LocalDateTime.now());
            producer.setUpdatedAt(LocalDateTime.now());
            
            int result = producerMapper.insert(producer);
            if (result > 0) {
                log.info("Successfully created producer: {}", producer.getId());
                return producer;
            } else {
                throw new RuntimeException("Failed to create producer");
            }
        } else {
            producer.setUpdatedAt(LocalDateTime.now());
            int result = producerMapper.update(producer);
            if (result > 0) {
                log.info("Successfully updated producer: {}", producer.getId());
                return producer;
            } else {
                throw new RuntimeException("Failed to update producer");
            }
        }
    }
    
    /**
     * Find producer by ID
     */
    @Cacheable(value = "producers", key = "#id")
    public Optional<Producer> findById(String id) {
        log.debug("Finding producer by ID: {}", id);
        return producerMapper.findById(id);
    }
    
    /**
     * Find producer by user ID
     */
    @Cacheable(value = "producers", key = "'userId:' + #userId")
    public Optional<Producer> findByUserId(String userId) {
        log.debug("Finding producer by user ID: {}", userId);
        return producerMapper.findByUserId(userId);
    }
    
    /**
     * Find all producers
     */
    public List<Producer> findAll(int page, int size) {
        log.debug("Finding all producers - page: {}, size: {}", page, size);
        int offset = page * size;
        return producerMapper.findAll(offset, size);
    }
    
    /**
     * Count total producers
     */
    @Cacheable(value = "producerStats", key = "'totalProducers'")
    public long count() {
        log.debug("Counting total producers");
        return producerMapper.count();
    }
    
    /**
     * Check if producer exists by user ID
     */
    public boolean existsByUserId(String userId) {
        log.debug("Checking if producer exists by user ID: {}", userId);
        return producerMapper.existsByUserId(userId);
    }
}