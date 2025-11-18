package com.example.api.gateway.config;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@AutoConfigureMockMvc
public class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void contextLoads_SecurityConfigBean() {
        // Just ensures Spring context loads without errors
    }

    @Test
    void publicEndpoint_ShouldBeAccessibleWithoutAuth() throws Exception {
        mockMvc.perform(get("/public/some-endpoint"))
               .andExpect(status().isOk()); // adjust URL according to your public paths
    }

    @Test
    void protectedEndpoint_ShouldReturnUnauthorizedWithoutAuth() throws Exception {
        mockMvc.perform(get("/api/protected"))
               .andExpect(status().isUnauthorized()); // adjust URL for a protected endpoint
    }

    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    void protectedEndpoint_ShouldBeAccessibleWithUserAuth() throws Exception {
        mockMvc.perform(get("/api/protected"))
               .andExpect(status().isOk()); // user with USER role can access
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void adminEndpoint_ShouldBeAccessibleWithAdminRole() throws Exception {
        mockMvc.perform(get("/api/admin"))
               .andExpect(status().isOk()); // adjust URL for admin endpoint
    }

    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    void adminEndpoint_ShouldReturnForbiddenForUserRole() throws Exception {
        mockMvc.perform(get("/api/admin"))
               .andExpect(status().isForbidden()); // USER role can't access admin endpoint
    }

    @Test
    void corsHeaders_ShouldBePresent() throws Exception {
        mockMvc.perform(get("/public/some-endpoint")
                .header("Origin", "http://localhost:3000"))
               .andExpect(status().isOk())
               .andExpect(result -> {
                   String corsHeader = result.getResponse().getHeader("Access-Control-Allow-Origin");
                   assert corsHeader != null && corsHeader.equals("http://localhost:3000");
               });
    }
}
