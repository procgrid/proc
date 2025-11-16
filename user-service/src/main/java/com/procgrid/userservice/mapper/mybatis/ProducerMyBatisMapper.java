package com.procgrid.userservice.mapper.mybatis;

import com.procgrid.userservice.model.Producer;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * MyBatis mapper interface for Producer entity
 */
@Mapper
public interface ProducerMyBatisMapper {
    
    /**
     * Insert a new producer
     */
    int insert(Producer producer);
    
    /**
     * Update an existing producer
     */
    int update(Producer producer);
    
    /**
     * Find producer by ID
     */
    Optional<Producer> findById(@Param("id") String id);
    
    /**
     * Find producer by user ID
     */
    Optional<Producer> findByUserId(@Param("userId") String userId);
    
    /**
     * Find all producers with pagination
     */
    List<Producer> findAll(@Param("offset") int offset, @Param("limit") int limit);
    
    /**
     * Find producers by verification status
     */
    List<Producer> findByVerificationStatus(@Param("status") Producer.VerificationStatus status,
                                          @Param("offset") int offset, 
                                          @Param("limit") int limit);
    
    /**
     * Find producers by location
     */
    List<Producer> findByLocation(@Param("city") String city,
                                 @Param("state") String state,
                                 @Param("country") String country,
                                 @Param("offset") int offset, 
                                 @Param("limit") int limit);
    
    /**
     * Find producers within geographic radius
     */
    List<Producer> findByGeoLocation(@Param("latitude") BigDecimal latitude,
                                   @Param("longitude") BigDecimal longitude,
                                   @Param("radiusKm") Double radiusKm,
                                   @Param("offset") int offset, 
                                   @Param("limit") int limit);
    
    /**
     * Search producers by farm name or crop specialization
     */
    List<Producer> searchProducers(@Param("searchTerm") String searchTerm,
                                 @Param("offset") int offset, 
                                 @Param("limit") int limit);
    
    /**
     * Find producers by crop specialization
     */
    List<Producer> findByCropSpecialization(@Param("cropSpecialization") String cropSpecialization,
                                          @Param("offset") int offset, 
                                          @Param("limit") int limit);
    
    /**
     * Find producers by certifications
     */
    List<Producer> findByCertifications(@Param("certification") String certification,
                                      @Param("offset") int offset, 
                                      @Param("limit") int limit);
    
    /**
     * Find producers by farm size range
     */
    List<Producer> findByFarmSizeRange(@Param("minSize") BigDecimal minSize,
                                     @Param("maxSize") BigDecimal maxSize,
                                     @Param("offset") int offset, 
                                     @Param("limit") int limit);
    
    /**
     * Find producers by years of experience range
     */
    List<Producer> findByExperienceRange(@Param("minExperience") Integer minExperience,
                                       @Param("maxExperience") Integer maxExperience,
                                       @Param("offset") int offset, 
                                       @Param("limit") int limit);
    
    /**
     * Count total producers
     */
    long count();
    
    /**
     * Count producers by verification status
     */
    long countByVerificationStatus(@Param("status") Producer.VerificationStatus status);
    
    /**
     * Count producers by location
     */
    long countByLocation(@Param("city") String city,
                        @Param("state") String state,
                        @Param("country") String country);
    
    /**
     * Update verification status
     */
    int updateVerificationStatus(@Param("id") String id, 
                               @Param("status") Producer.VerificationStatus status,
                               @Param("notes") String notes);
    
    /**
     * Update farm details
     */
    int updateFarmDetails(@Param("id") String id,
                        @Param("farmName") String farmName,
                        @Param("farmDescription") String farmDescription,
                        @Param("farmSize") BigDecimal farmSize,
                        @Param("farmingPractices") String farmingPractices,
                        @Param("cropSpecialization") String cropSpecialization);
    
    /**
     * Update farm location
     */
    int updateFarmLocation(@Param("id") String id,
                         @Param("farmAddress") String farmAddress,
                         @Param("farmCity") String farmCity,
                         @Param("farmState") String farmState,
                         @Param("farmPincode") String farmPincode,
                         @Param("farmCountry") String farmCountry,
                         @Param("latitude") BigDecimal latitude,
                         @Param("longitude") BigDecimal longitude);
    
    /**
     * Update banking details
     */
    int updateBankingDetails(@Param("id") String id,
                           @Param("accountHolderName") String accountHolderName,
                           @Param("accountNumber") String accountNumber,
                           @Param("ifscCode") String ifscCode,
                           @Param("bankName") String bankName,
                           @Param("bankBranch") String bankBranch);
    
    /**
     * Update contact details
     */
    int updateContactDetails(@Param("id") String id,
                           @Param("contactPerson") String contactPerson,
                           @Param("contactPhone") String contactPhone,
                           @Param("contactEmail") String contactEmail);
    
    /**
     * Update certifications
     */
    int updateCertifications(@Param("id") String id,
                           @Param("certifications") String certifications);
    
    /**
     * Check if producer exists by user ID
     */
    boolean existsByUserId(@Param("userId") String userId);
    
    /**
     * Get verified producers count
     */
    long getVerifiedProducersCount();
    
    /**
     * Get pending verification producers count
     */
    long getPendingVerificationCount();
    
    /**
     * Delete producer
     */
    int delete(@Param("id") String id);
}