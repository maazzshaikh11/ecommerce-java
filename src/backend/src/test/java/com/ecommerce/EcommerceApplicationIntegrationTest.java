package com.ecommerce;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import org.springframework.test.context.ActiveProfiles;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class EcommerceApplicationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void healthEndpoint_shouldReturnHealthy() throws Exception {
        mockMvc.perform(get("/api/health"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("Healthy"))
            .andExpect(jsonPath("$.db").value(1));
    }

    @Test
    void featuredProducts_shouldReturnList() throws Exception {
        mockMvc.perform(get("/api/products/featured"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(4));
    }

    @Test
    void getCategories_shouldReturnList() throws Exception {
        mockMvc.perform(get("/api/categories"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void login_withValidCredentials_shouldReturnToken() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                .contentType("application/json")
                .content("{\"email\":\"test@example.com\",\"password\":\"password\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.token").exists());
    }

    @Test
    void login_withInvalidCredentials_shouldReturn401() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                .contentType("application/json")
                .content("{\"email\":\"bad@example.com\",\"password\":\"wrong\"}"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void cart_shouldRequireAuth() throws Exception {
        mockMvc.perform(get("/api/cart"))
            .andExpect(status().isUnauthorized());
    }
}
