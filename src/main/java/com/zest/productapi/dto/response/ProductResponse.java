package com.zest.productapi.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductResponse {

    private Long id;
    private String productName;
    private String createdBy;
    private LocalDateTime createdOn;
    private String modifiedBy;
    private LocalDateTime modifiedOn;
    private List<ItemResponse> items;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ItemResponse {
        private Long id;
        private Integer quantity;
    }
}
