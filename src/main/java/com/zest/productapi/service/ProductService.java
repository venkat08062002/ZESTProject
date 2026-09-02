package com.zest.productapi.service;

import com.zest.productapi.dto.request.ProductRequest;
import com.zest.productapi.dto.response.PageResponse;
import com.zest.productapi.dto.response.ProductResponse;
import com.zest.productapi.entity.Item;
import com.zest.productapi.entity.Product;
import com.zest.productapi.exception.ResourceNotFoundException;
import com.zest.productapi.mapper.ProductMapper;
import com.zest.productapi.repository.ItemRepository;
import com.zest.productapi.repository.ProductRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final ItemRepository itemRepository;
    private final ProductMapper productMapper;
    private final AuditService auditService;

    public ProductService(ProductRepository productRepository,
                          ItemRepository itemRepository,
                          ProductMapper productMapper,
                          AuditService auditService) {
        this.productRepository = productRepository;
        this.itemRepository = itemRepository;
        this.productMapper = productMapper;
        this.auditService = auditService;
    }

    @Transactional(readOnly = true)
    public PageResponse<ProductResponse> getAllProducts(String search, Pageable pageable) {
        Page<Product> page = StringUtils.hasText(search)
                ? productRepository.findByProductNameContainingIgnoreCase(search, pageable)
                : productRepository.findAll(pageable);

        Page<ProductResponse> mapped = page.map(product -> {
            List<Item> items = itemRepository.findByProductId(product.getId());
            return productMapper.toResponse(product, items);
        });

        return productMapper.toPageResponse(mapped);
    }

    @Transactional(readOnly = true)
    public ProductResponse getProductById(Long id) {
        Product product = findProductOrThrow(id);
        List<Item> items = itemRepository.findByProductId(id);
        return productMapper.toResponse(product, items);
    }

    @Transactional(readOnly = true)
    public PageResponse<ProductResponse.ItemResponse> getProductItems(Long productId, Pageable pageable) {
        findProductOrThrow(productId);
        Page<Item> items = itemRepository.findByProductId(productId, pageable);
        Page<ProductResponse.ItemResponse> mapped = items.map(productMapper::toItemResponse);
        return productMapper.toPageResponse(mapped);
    }

    @Transactional
    public ProductResponse createProduct(ProductRequest request, String username) {
        Product product = Product.builder()
                .productName(request.getProductName())
                .createdBy(username)
                .build();

        Product savedProduct = productRepository.save(product);
        List<Item> items = saveItems(savedProduct, request.getItems());

        auditService.logProductAction("CREATE", savedProduct.getId(), username);
        return productMapper.toResponse(savedProduct, items);
    }

    @Transactional
    public ProductResponse updateProduct(Long id, ProductRequest request, String username) {
        Product product = findProductOrThrow(id);
        product.setProductName(request.getProductName());
        product.setModifiedBy(username);

        Product updatedProduct = productRepository.save(product);
        itemRepository.findByProductId(id).forEach(itemRepository::delete);
        List<Item> items = saveItems(updatedProduct, request.getItems());

        auditService.logProductAction("UPDATE", updatedProduct.getId(), username);
        return productMapper.toResponse(updatedProduct, items);
    }

    @Transactional
    public void deleteProduct(Long id, String username) {
        Product product = findProductOrThrow(id);
        itemRepository.findByProductId(id).forEach(itemRepository::delete);
        productRepository.delete(product);
        auditService.logProductAction("DELETE", id, username);
    }

    private List<Item> saveItems(Product product, List<ProductRequest.ItemRequest> itemRequests) {
        if (itemRequests == null || itemRequests.isEmpty()) {
            return new ArrayList<>();
        }

        List<Item> items = itemRequests.stream()
                .map(itemRequest -> Item.builder()
                        .product(product)
                        .quantity(itemRequest.getQuantity())
                        .build())
                .toList();

        return itemRepository.saveAll(items);
    }

    private Product findProductOrThrow(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
    }
}
