package com.zest.productapi.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zest.productapi.dto.request.ProductRequest;
import com.zest.productapi.dto.response.PageResponse;
import com.zest.productapi.dto.response.ProductResponse;
import com.zest.productapi.service.ProductService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ProductService productService;

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void createProduct_shouldReturn201() throws Exception {
        ProductRequest request = ProductRequest.builder()
                .productName("New Product")
                .items(List.of(ProductRequest.ItemRequest.builder().quantity(10).build()))
                .build();

        ProductResponse response = ProductResponse.builder()
                .id(1L)
                .productName("New Product")
                .createdBy("admin")
                .createdOn(LocalDateTime.now())
                .build();

        when(productService.createProduct(any(ProductRequest.class), eq("admin"))).thenReturn(response);

        mockMvc.perform(post("/api/v1/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.productName").value("New Product"));
    }

    @Test
    @WithMockUser(username = "user", roles = "USER")
    void getAllProducts_shouldReturnPaginatedList() throws Exception {
        PageResponse<ProductResponse> pageResponse = PageResponse.<ProductResponse>builder()
                .content(List.of(ProductResponse.builder().id(1L).productName("Product A").build()))
                .page(0)
                .size(10)
                .totalElements(1)
                .totalPages(1)
                .first(true)
                .last(true)
                .build();

        when(productService.getAllProducts(eq(null), any(PageRequest.class))).thenReturn(pageResponse);

        mockMvc.perform(get("/api/v1/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].productName").value("Product A"))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void getAllProducts_shouldReturn401_whenUnauthenticated() throws Exception {
        mockMvc.perform(get("/api/v1/products"))
                .andExpect(status().isUnauthorized());
    }
}
