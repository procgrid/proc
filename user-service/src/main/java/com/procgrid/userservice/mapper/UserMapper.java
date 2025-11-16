package com.procgrid.userservice.mapper;

import com.procgrid.userservice.dto.request.ProducerRegistrationRequest;
import com.procgrid.userservice.dto.request.BuyerRegistrationRequest;
import com.procgrid.userservice.dto.response.UserResponse;
import com.procgrid.userservice.dto.response.ProducerResponse;
import com.procgrid.userservice.dto.response.BuyerResponse;
import com.procgrid.userservice.model.User;
import com.procgrid.userservice.model.Producer;
import com.procgrid.userservice.model.Buyer;
import org.mapstruct.*;

/**
 * MapStruct mapper for User entities and DTOs
 */
@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface UserMapper {
    
    /**
     * Maps ProducerRegistrationRequest to User entity
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "role", constant = "PRODUCER")
    @Mapping(target = "status", constant = "PENDING_VERIFICATION")
    @Mapping(target = "emailVerified", constant = "false")
    @Mapping(target = "phoneVerified", constant = "false")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    User toUserFromProducerRequest(ProducerRegistrationRequest request);
    
    /**
     * Maps ProducerRegistrationRequest to Producer entity
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "userId", ignore = true)
    @Mapping(target = "verificationStatus", constant = "PENDING")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Producer toProducerFromRequest(ProducerRegistrationRequest request);
    
    /**
     * Maps BuyerRegistrationRequest to User entity
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "role", constant = "BUYER")
    @Mapping(target = "status", constant = "PENDING_VERIFICATION")
    @Mapping(target = "emailVerified", constant = "false")
    @Mapping(target = "phoneVerified", constant = "false")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    User toUserFromBuyerRequest(BuyerRegistrationRequest request);
    
    /**
     * Maps BuyerRegistrationRequest to Buyer entity
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "userId", ignore = true)
    @Mapping(target = "verificationStatus", constant = "PENDING")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Buyer toBuyerFromRequest(BuyerRegistrationRequest request);
    
    /**
     * Maps User entity to UserResponse DTO
     */
    @Mapping(target = "fullName", expression = "java(user.getFullName())")
    UserResponse toUserResponse(User user);
    
    /**
     * Maps Producer entity to ProducerResponse DTO
     */
    @Mapping(target = "fullFarmAddress", expression = "java(producer.getFullFarmAddress())")
    @Mapping(target = "maskedBankAccountNumber", expression = "java(maskBankAccountNumber(producer.getBankAccountNumber()))")
    ProducerResponse toProducerResponse(Producer producer);
    
    /**
     * Maps Buyer entity to BuyerResponse DTO
     */
    @Mapping(target = "fullBusinessAddress", expression = "java(buyer.getFullBusinessAddress())")
    @Mapping(target = "maskedBankAccountNumber", expression = "java(maskBankAccountNumber(buyer.getBankAccountNumber()))")
    @Mapping(target = "isCorporate", expression = "java(buyer.isCorporate())")
    BuyerResponse toBuyerResponse(Buyer buyer);
    
    /**
     * Maps User and Producer entities to combined ProducerResponse
     */
    @Mapping(target = "userId", source = "producer.userId")
    @Mapping(target = "email", source = "user.email")
    @Mapping(target = "firstName", source = "user.firstName")
    @Mapping(target = "lastName", source = "user.lastName")
    @Mapping(target = "fullName", expression = "java(user.getFullName())")
    @Mapping(target = "phone", source = "user.phone")
    @Mapping(target = "profileImageUrl", source = "user.profileImageUrl")
    @Mapping(target = "location", source = "user.location")
    @Mapping(target = "gstNumber", source = "user.gstNumber")
    @Mapping(target = "emailVerified", source = "user.emailVerified")
    @Mapping(target = "phoneVerified", source = "user.phoneVerified")
    @Mapping(target = "fullFarmAddress", expression = "java(producer.getFullFarmAddress())")
    @Mapping(target = "maskedBankAccountNumber", expression = "java(maskBankAccountNumber(producer.getBankAccountNumber()))")
    @Mapping(target = "createdAt", source = "producer.createdAt")
    @Mapping(target = "updatedAt", source = "producer.updatedAt")
    ProducerResponse toCombinedProducerResponse(User user, Producer producer);
    
    /**
     * Maps User and Buyer entities to combined BuyerResponse
     */
    @Mapping(target = "userId", source = "buyer.userId")
    @Mapping(target = "email", source = "user.email")
    @Mapping(target = "firstName", source = "user.firstName")
    @Mapping(target = "lastName", source = "user.lastName")
    @Mapping(target = "fullName", expression = "java(user.getFullName())")
    @Mapping(target = "phone", source = "user.phone")
    @Mapping(target = "profileImageUrl", source = "user.profileImageUrl")
    @Mapping(target = "location", source = "user.location")
    @Mapping(target = "gstNumber", source = "user.gstNumber")
    @Mapping(target = "emailVerified", source = "user.emailVerified")
    @Mapping(target = "phoneVerified", source = "user.phoneVerified")
    @Mapping(target = "fullBusinessAddress", expression = "java(buyer.getFullBusinessAddress())")
    @Mapping(target = "maskedBankAccountNumber", expression = "java(maskBankAccountNumber(buyer.getBankAccountNumber()))")
    @Mapping(target = "isCorporate", expression = "java(buyer.isCorporate())")
    @Mapping(target = "createdAt", source = "buyer.createdAt")
    @Mapping(target = "updatedAt", source = "buyer.updatedAt")
    BuyerResponse toCombinedBuyerResponse(User user, Buyer buyer);
    
    /**
     * Helper method to mask bank account number for security
     */
    default String maskBankAccountNumber(String accountNumber) {
        if (accountNumber == null || accountNumber.length() < 4) {
            return accountNumber;
        }
        int visibleDigits = 4;
        String masked = "*".repeat(accountNumber.length() - visibleDigits);
        return masked + accountNumber.substring(accountNumber.length() - visibleDigits);
    }
}