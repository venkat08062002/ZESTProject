package com.zest.productapi.controller;

import com.zest.productapi.dto.request.ProductRequest;
import com.zest.productapi.dto.response.PageResponse;
import com.zest.productapi.dto.response.ProductResponse;
import com.zest.productapi.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/products")
@Tag(name = "Products", description = "Product CRUD operations")
@SecurityRequirement(name = "bearerAuth")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    @Operation(summary = "Get all products with pagination")
    public ResponseEntity<PageResponse<ProductResponse>> getAllProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdOn") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(required = false) String search) {

        Sort sort = sortDir.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        return ResponseEntity.ok(productService.getAllProducts(search, pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get product by ID")
    public ResponseEntity<ProductResponse> getProductById(@PathVariable Long id) {
        return ResponseEntity.ok(productService.getProductById(id));
    }

    @GetMapping("/{id}/items")
    @Operation(summary = "Get items for a product with pagination")
    public ResponseEntity<PageResponse<ProductResponse.ItemResponse>> getProductItems(
            @PathVariable Long id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("id").ascending());
        return ResponseEntity.ok(productService.getProductItems(id, pageable));
    }

    @PostMapping
    @Operation(summary = "Create a new product")
    public ResponseEntity<ProductResponse> createProduct(
            @Valid @RequestBody ProductRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        ProductResponse response = productService.createProduct(request, userDetails.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing product")
    public ResponseEntity<ProductResponse> updateProduct(
            @PathVariable Long id,
            @Valid @RequestBody ProductRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        return ResponseEntity.ok(productService.updateProduct(id, request, userDetails.getUsername()));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a product")
    public ResponseEntity<Void> deleteProduct(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {

        productService.deleteProduct(id, userDetails.getUsername());
        return ResponseEntity.noContent().build();
    }
}
