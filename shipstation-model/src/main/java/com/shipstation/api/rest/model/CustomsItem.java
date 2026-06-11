package com.shipstation.api.rest.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomsItem {
    private Long customsItemId;
    private String description;
    private Integer quantity;
    private Double value;
    private String harmonizedTariffCode;
    private String countryOfOrigin;
}
