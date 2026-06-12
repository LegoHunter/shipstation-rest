package com.shipstation.api.rest.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdvancedOptions {
    private Long warehouseId;
    private Boolean nonMachinable;
    private Boolean saturdayDelivery;
    private Boolean containsAlcohol;
    private Long storeId;
    private String customField1;
    private String customField2;
    private String customField3;
    private String source;
    private Boolean mergedOrSplit;
    private Long mergedIds;
    private Long parentId;
    private String billToParty;
    private String billToAccount;
    private String billToPostalCode;
    private String billToCountryCode;
}
