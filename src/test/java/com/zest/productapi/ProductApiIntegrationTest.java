package com.zest.productapi;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ProductApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String adminToken;

    @BeforeEach
    void setUp() throws Exception {
        MvcResult loginResult = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"admin","password":"admin123"}
                                """))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode loginJson = objectMapper.readTree(loginResult.getResponse().getContentAsString());
        adminToken = loginJson.get("accessToken").asText();
    }

    @Test
    void fullProductCrudFlow_shouldWork() throws Exception {
        MvcResult createResult = mockMvc.perform(post("/api/v1/products")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "productName": "Integration Test Product",
                                  "items": [{"quantity": 3}, {"quantity": 7}]
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.productName").value("Integration Test Product"))
                .andExpect(jsonPath("$.items.length()").value(2))
                .andReturn();

        JsonNode created = objectMapper.readTree(createResult.getResponse().getContentAsString());
        long productId = created.get("id").asLong();

        mockMvc.perform(get("/api/v1/products/" + productId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(productId));

        mockMvc.perform(get("/api/v1/products/" + productId + "/items")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2));

        mockMvc.perform(put("/api/v1/products/" + productId)
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "productName": "Updated Product",
                                  "items": [{"quantity": 15}]
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productName").value("Updated Product"))
                .andExpect(jsonPath("$.modifiedBy").value("admin"));

        mockMvc.perform(delete("/api/v1/products/" + productId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/v1/products/" + productId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNotFound());
    }

    @Test
    void refreshToken_shouldRotateTokens() throws Exception {
        MvcResult loginResult = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"user","password":"user123"}
                                """))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode loginJson = objectMapper.readTree(loginResult.getResponse().getContentAsString());
        String refreshToken = loginJson.get("refreshToken").asText();

        MvcResult refreshResult = mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refreshToken\": \"" + refreshToken + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.refreshToken").exists())
                .andReturn();

        JsonNode refreshJson = objectMapper.readTree(refreshResult.getResponse().getContentAsString());
        assertThat(refreshJson.get("refreshToken").asText()).isNotEqualTo(refreshToken);

        mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refreshToken\": \"" + refreshToken + "\"}"))
                .andExpect(status().isUnauthorized());
    }
}
