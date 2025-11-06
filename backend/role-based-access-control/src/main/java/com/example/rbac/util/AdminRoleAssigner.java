package com.example.rbac.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.HashMap;
import java.util.Map;

/**
 * Utility class to assign admin roles to users via Supabase Admin API.
 * 
 * Usage example (as a Spring Bean):
 * 
 * @Autowired
 * private AdminRoleAssigner adminRoleAssigner;
 * 
 * adminRoleAssigner.assignAdminRole("user-id");
 */
@Component
public class AdminRoleAssigner {

    @Value("${supabase.url:}")
    private String supabaseUrl;

    @Value("${supabase.service.role.key:}")
    private String serviceRoleKey;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Assign admin role to a user by their user ID.
     * 
     * @param userId The Supabase user ID
     * @return true if successful, false otherwise
     */
    public boolean assignAdminRole(String userId) {
        if (supabaseUrl == null || supabaseUrl.isEmpty()) {
            System.err.println("Error: supabase.url is not configured");
            return false;
        }

        if (serviceRoleKey == null || serviceRoleKey.isEmpty()) {
            System.err.println("Error: supabase.service.role.key is not configured");
            return false;
        }

        if (userId == null || userId.trim().isEmpty()) {
            System.err.println("Error: User ID is required");
            return false;
        }

        try {
            // Build the request URL
            String url = supabaseUrl + "/auth/v1/admin/users/" + userId;

            // Set up headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("apikey", serviceRoleKey);
            headers.set("Authorization", "Bearer " + serviceRoleKey);

            // Build the request body
            Map<String, Object> appMetadata = new HashMap<>();
            appMetadata.put("role", "admin");

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("app_metadata", appMetadata);

            // Create the HTTP entity
            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);

            // Make the API call
            System.out.println("Calling Supabase Admin API to assign admin role...");
            ResponseEntity<String> response = restTemplate.exchange(
                    url,
                    HttpMethod.PUT,
                    requestEntity,
                    String.class
            );

            if (response.getStatusCode().is2xxSuccessful()) {
                System.out.println("✓ Successfully assigned admin role to user: " + userId);
                return true;
            } else {
                System.err.println("Failed to assign admin role. Status: " + response.getStatusCode());
                System.err.println("Response: " + response.getBody());
                return false;
            }

        } catch (Exception e) {
            System.err.println("Error assigning admin role: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Find user by email and assign admin role.
     * 
     * @param email The user's email
     * @return true if successful, false otherwise
     */
    public boolean assignAdminRoleByEmail(String email) {
        if (supabaseUrl == null || supabaseUrl.isEmpty()) {
            System.err.println("Error: supabase.url is not configured");
            return false;
        }

        if (serviceRoleKey == null || serviceRoleKey.isEmpty()) {
            System.err.println("Error: supabase.service.role.key is not configured");
            return false;
        }

        if (email == null || email.trim().isEmpty()) {
            System.err.println("Error: Email is required");
            return false;
        }

        try {
            // First, find the user by email
            String listUrl = supabaseUrl + "/auth/v1/admin/users";
            
            HttpHeaders headers = new HttpHeaders();
            headers.set("apikey", serviceRoleKey);
            headers.set("Authorization", "Bearer " + serviceRoleKey);

            HttpEntity<Void> requestEntity = new HttpEntity<>(headers);
            
            System.out.println("Looking up user by email: " + email);
            ResponseEntity<String> listResponse = restTemplate.exchange(
                    listUrl,
                    HttpMethod.GET,
                    requestEntity,
                    String.class
            );

            if (!listResponse.getStatusCode().is2xxSuccessful()) {
                System.err.println("Failed to list users: " + listResponse.getStatusCode());
                return false;
            }

            // Parse the response to find the user
            JsonNode root = objectMapper.readTree(listResponse.getBody());
            JsonNode users = root.get("users");

            if (users == null || !users.isArray()) {
                System.err.println("No users found in response");
                return false;
            }

            String userId = null;
            for (JsonNode user : users) {
                if (user.has("email") && user.get("email").asText().equals(email)) {
                    userId = user.get("id").asText();
                    break;
                }
            }

            if (userId == null) {
                System.err.println("User with email " + email + " not found");
                return false;
            }

            System.out.println("Found user: " + userId);
            
            // Now assign the admin role
            return assignAdminRole(userId);

        } catch (Exception e) {
            System.err.println("Error finding user by email: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Main method to run as a standalone application.
     */
    public static void main(String[] args) {
        if (args.length < 1) {
            System.err.println("Usage: java AdminRoleAssigner <USER_ID or EMAIL>");
            System.exit(1);
        }

        AdminRoleAssigner assigner = new AdminRoleAssigner();
        assigner.supabaseUrl = System.getenv("SUPABASE_URL");
        assigner.serviceRoleKey = System.getenv("SUPABASE_SERVICE_ROLE_KEY");

        if (assigner.supabaseUrl == null || assigner.supabaseUrl.isEmpty()) {
            System.err.println("Error: SUPABASE_URL environment variable is not set");
            System.exit(1);
        }

        if (assigner.serviceRoleKey == null || assigner.serviceRoleKey.isEmpty()) {
            System.err.println("Error: SUPABASE_SERVICE_ROLE_KEY environment variable is not set");
            System.exit(1);
        }

        String identifier = args[0];
        boolean success;

        if (identifier.contains("@")) {
            // It's an email
            System.out.println("Assigning admin role by email...");
            success = assigner.assignAdminRoleByEmail(identifier);
        } else {
            // It's a user ID
            System.out.println("Assigning admin role by user ID...");
            success = assigner.assignAdminRole(identifier);
        }

        if (success) {
            System.out.println("\n✓ Role assignment completed successfully!");
            System.out.println("Note: User must log out and log back in for changes to take effect.");
            System.exit(0);
        } else {
            System.exit(1);
        }
    }
}

