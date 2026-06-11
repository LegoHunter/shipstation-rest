package com.shipstation.api.rest.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderItem {
    private Long orderItemId;
    private String lineItemKey;
    private String sku;
    private String name;
    private String imageUrl;
    private Weight weight;
    private Integer quantity;
    private Double unitPrice;
    private Double taxAmount;
    private Double shippingAmount;
    private String warehouseLocation;
    private ItemOption[] options;
    private String fulfillmentSku;
    private Boolean adjustment;
    private String upc;
    private OffsetDateTime createDate;
    private OffsetDateTime modifyDate;
}
