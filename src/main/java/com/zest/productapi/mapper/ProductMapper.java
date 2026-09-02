package com.zest.productapi.mapper;

import com.zest.productapi.dto.response.PageResponse;
import com.zest.productapi.dto.response.ProductResponse;
import com.zest.productapi.entity.Item;
import com.zest.productapi.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
public class ProductMapper {

    public ProductResponse toResponse(Product product, List<Item> items) {
        return ProductResponse.builder()
                .id(product.getId())
                .productName(product.getProductName())
                .createdBy(product.getCreatedBy())
                .createdOn(product.getCreatedOn())
                .modifiedBy(product.getModifiedBy())
                .modifiedOn(product.getModifiedOn())
                .items(items == null ? Collections.emptyList() : items.stream().map(this::toItemResponse).toList())
                .build();
    }

    public ProductResponse toResponse(Product product) {
        return toResponse(product, Collections.emptyList());
    }

    public ProductResponse.ItemResponse toItemResponse(Item item) {
        return ProductResponse.ItemResponse.builder()
                .id(item.getId())
                .quantity(item.getQuantity())
                .build();
    }

    public <T> PageResponse<T> toPageResponse(Page<T> page) {
        return PageResponse.<T>builder()
                .content(page.getContent())
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .first(page.isFirst())
                .last(page.isLast())
                .build();
    }
}
