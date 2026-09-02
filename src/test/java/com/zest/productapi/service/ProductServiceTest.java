package com.zest.productapi.service;

import com.zest.productapi.dto.request.ProductRequest;
import com.zest.productapi.dto.response.ProductResponse;
import com.zest.productapi.entity.Product;
import com.zest.productapi.exception.ResourceNotFoundException;
import com.zest.productapi.mapper.ProductMapper;
import com.zest.productapi.repository.ItemRepository;
import com.zest.productapi.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private ProductMapper productMapper;

    @Mock
    private AuditService auditService;

    @InjectMocks
    private ProductService productService;

    private Product product;
    private ProductRequest productRequest;
    private ProductResponse productResponse;

    @BeforeEach
    void setUp() {
        product = Product.builder()
                .id(1L)
                .productName("Test Product")
                .createdBy("admin")
                .createdOn(LocalDateTime.now())
                .build();

        productRequest = ProductRequest.builder()
                .productName("Test Product")
                .items(List.of(ProductRequest.ItemRequest.builder().quantity(5).build()))
                .build();

        productResponse = ProductResponse.builder()
                .id(1L)
                .productName("Test Product")
                .createdBy("admin")
                .build();
    }

    @Test
    void getProductById_shouldReturnProduct_whenExists() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(itemRepository.findByProductId(1L)).thenReturn(List.of());
        when(productMapper.toResponse(product, List.of())).thenReturn(productResponse);

        ProductResponse result = productService.getProductById(1L);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getProductName()).isEqualTo("Test Product");
    }

    @Test
    void getProductById_shouldThrow_whenNotFound() {
        when(productRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.getProductById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Product not found");
    }

    @Test
    void createProduct_shouldSaveAndReturnProduct() {
        when(productRepository.save(any(Product.class))).thenReturn(product);
        when(itemRepository.saveAll(any())).thenReturn(List.of());
        when(productMapper.toResponse(any(Product.class), any())).thenReturn(productResponse);

        ProductResponse result = productService.createProduct(productRequest, "admin");

        assertThat(result.getProductName()).isEqualTo("Test Product");
        verify(auditService).logProductAction("CREATE", 1L, "admin");
    }

    @Test
    void deleteProduct_shouldDelete_whenExists() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(itemRepository.findByProductId(1L)).thenReturn(List.of());

        productService.deleteProduct(1L, "admin");

        verify(productRepository).delete(product);
        verify(auditService).logProductAction("DELETE", 1L, "admin");
    }

    @Test
    void getAllProducts_shouldReturnPaginatedResults() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Product> page = new PageImpl<>(List.of(product));
        when(productRepository.findAll(pageable)).thenReturn(page);
        when(itemRepository.findByProductId(1L)).thenReturn(List.of());
        when(productMapper.toResponse(product, List.of())).thenReturn(productResponse);
        when(productMapper.toPageResponse(any(Page.class))).thenCallRealMethod();

        var result = productService.getAllProducts(null, pageable);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getTotalElements()).isEqualTo(1);
    }
}
