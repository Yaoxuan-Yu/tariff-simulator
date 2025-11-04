package com.example.tariff.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;

@Component
// this method helps to verify the user based on the jwt token issued by supabase 
public class SupabaseJwtAuthenticationFilter extends OncePerRequestFilter {

    @Value("${supabase.jwt.secret}")
    private String jwtSecret;

    @Value("${supabase.jwt.audience:}")
    private String expectedAudience;

    @Value("${supabase.jwt.issuer:}")
    private String expectedIssuer;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, 
                                  FilterChain filterChain) throws ServletException, IOException {
        
        String path = request.getRequestURI();

        if (path.equals("/swagger-ui.html") || path.startsWith("/swagger-ui/")) {
            filterChain.doFilter(request, response);
            return;
        }

        String authHeader = request.getHeader("Authorization");
        
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            
            try {
                Claims claims = Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
                
                String userId = claims.getSubject();
                String email = claims.get("email", String.class);
                String issuer = claims.getIssuer();
                Object audClaim = claims.get("aud");
                boolean audienceOk = true;
                if (expectedAudience != null && !expectedAudience.isBlank()) {
                    if (audClaim instanceof String audStr) {
                        audienceOk = expectedAudience.equals(audStr);
                    } else if (audClaim instanceof List<?> audList) {
                        audienceOk = audList.stream().anyMatch(v -> expectedAudience.equals(String.valueOf(v)));
                    }
                }
                boolean issuerOk = expectedIssuer == null || expectedIssuer.isBlank() || expectedIssuer.equals(issuer);
                
                if (userId != null && email != null && audienceOk && issuerOk) {

                    // Extract role from JWT claims (app_metadata.role from Supabase Admin API)
                    String userRole = extractRoleFromClaims(claims);
                    
                    List<SimpleGrantedAuthority> authorities = Collections.singletonList(
                        new SimpleGrantedAuthority(userRole)
                    );
                    
                    UsernamePasswordAuthenticationToken authentication = 
                        new UsernamePasswordAuthenticationToken(userId, null, authorities);
                    
                    authentication.setDetails(new SupabaseUserDetails(userId, email));
                    
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
                
            } catch (Exception e) {
                SecurityContextHolder.clearContext();
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json");
                response.getWriter().write("{\"error\":\"Invalid token\",\"message\":\"" + e.getMessage() + "\"}");
                return;
            }
        }
        
        filterChain.doFilter(request, response);
    }
    
    private SecretKey getSigningKey() {
        byte[] keyBytes = jwtSecret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    private String extractRoleFromClaims(Claims claims) {
        // Check app_metadata.role (set by Supabase Admin API)
        Object appMetadata = claims.get("app_metadata");
        if (appMetadata instanceof java.util.Map) {
            @SuppressWarnings("unchecked")
            java.util.Map<String, Object> appMetadataMap = (java.util.Map<String, Object>) appMetadata;
            Object role = appMetadataMap.get("role");
            if (role instanceof String roleStr && roleStr.equalsIgnoreCase("admin")) {
                return "ROLE_ADMIN";
            }
        }

        // Check user_metadata.role (alternative location)
        Object userMetadata = claims.get("user_metadata");
        if (userMetadata instanceof java.util.Map) {
            @SuppressWarnings("unchecked")
            java.util.Map<String, Object> userMetadataMap = (java.util.Map<String, Object>) userMetadata;
            Object role = userMetadataMap.get("role");
            if (role instanceof String roleStr && roleStr.equalsIgnoreCase("admin")) {
                return "ROLE_ADMIN";
            }
        }

        // Default to general user
        return "ROLE_USER";
    }
    
    public static class SupabaseUserDetails implements Serializable {
        private static final long serialVersionUID = 1L;
        
        private final String userId;
        private final String email;
        
        public SupabaseUserDetails(String userId, String email) {
            this.userId = userId;
            this.email = email;
        }
        
        public String getUserId() {
            return userId;
        }
        
        public String getEmail() {
            return email;
        }
    }
}
