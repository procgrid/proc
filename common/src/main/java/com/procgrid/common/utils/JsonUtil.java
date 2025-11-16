package com.procgrid.common.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;

/**
 * JSON utility class for serialization and deserialization
 */
@Slf4j
public class JsonUtil {
    
    private static final ObjectMapper objectMapper = new ObjectMapper();
    
    static {
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }
    
    /**
     * Convert object to JSON string
     */
    public static String toJson(Object object) {
        try {
            return objectMapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            log.error("Error converting object to JSON: {}", e.getMessage());
            return null;
        }
    }
    
    /**
     * Convert object to pretty JSON string
     */
    public static String toPrettyJson(Object object) {
        try {
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(object);
        } catch (JsonProcessingException e) {
            log.error("Error converting object to pretty JSON: {}", e.getMessage());
            return null;
        }
    }
    
    /**
     * Convert JSON string to object
     */
    public static <T> T fromJson(String json, Class<T> valueType) {
        if (json == null || json.trim().isEmpty()) {
            return null;
        }
        try {
            return objectMapper.readValue(json, valueType);
        } catch (JsonProcessingException e) {
            log.error("Error parsing JSON to object: {}", e.getMessage());
            return null;
        }
    }
    
    /**
     * Convert JSON string to List
     */
    public static <T> List<T> fromJsonToList(String json, Class<T> elementType) {
        if (json == null || json.trim().isEmpty()) {
            return List.of();
        }
        try {
            return objectMapper.readValue(json, 
                objectMapper.getTypeFactory().constructCollectionType(List.class, elementType));
        } catch (JsonProcessingException e) {
            log.error("Error parsing JSON to List: {}", e.getMessage());
            return List.of();
        }
    }
    
    /**
     * Convert JSON string to Map
     */
    @SuppressWarnings("unchecked")
    public static Map<String, Object> fromJsonToMap(String json) {
        if (json == null || json.trim().isEmpty()) {
            return Map.of();
        }
        try {
            return objectMapper.readValue(json, Map.class);
        } catch (JsonProcessingException e) {
            log.error("Error parsing JSON to Map: {}", e.getMessage());
            return Map.of();
        }
    }
    
    /**
     * Convert object to Map
     */
    @SuppressWarnings("unchecked")
    public static Map<String, Object> toMap(Object object) {
        try {
            return objectMapper.convertValue(object, Map.class);
        } catch (Exception e) {
            log.error("Error converting object to Map: {}", e.getMessage());
            return Map.of();
        }
    }
    
    /**
     * Convert Map to object
     */
    public static <T> T fromMap(Map<String, Object> map, Class<T> valueType) {
        try {
            return objectMapper.convertValue(map, valueType);
        } catch (Exception e) {
            log.error("Error converting Map to object: {}", e.getMessage());
            return null;
        }
    }
    
    /**
     * Parse JSON string to JsonNode
     */
    public static JsonNode parseJson(String json) {
        if (json == null || json.trim().isEmpty()) {
            return null;
        }
        try {
            return objectMapper.readTree(json);
        } catch (JsonProcessingException e) {
            log.error("Error parsing JSON: {}", e.getMessage());
            return null;
        }
    }
    
    /**
     * Check if string is valid JSON
     */
    public static boolean isValidJson(String json) {
        if (json == null || json.trim().isEmpty()) {
            return false;
        }
        try {
            objectMapper.readTree(json);
            return true;
        } catch (JsonProcessingException e) {
            return false;
        }
    }
    
    /**
     * Deep clone object using JSON serialization
     */
    public static <T> T deepCopy(T object, Class<T> valueType) {
        try {
            String json = objectMapper.writeValueAsString(object);
            return objectMapper.readValue(json, valueType);
        } catch (JsonProcessingException e) {
            log.error("Error deep copying object: {}", e.getMessage());
            return null;
        }
    }
}