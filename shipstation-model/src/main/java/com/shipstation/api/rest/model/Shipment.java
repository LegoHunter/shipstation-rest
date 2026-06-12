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
public class Shipment {
    private Long shipmentId;
    private Long fulfillmentId;
    private Long orderId;
    private String orderNumber;
    private String userId;
    private String customerEmail;
    private String trackingNumber;
    private OffsetDateTime createDate;
    private OffsetDateTime shipDate;
    private OffsetDateTime voidDate;
    private OffsetDateTime deliveryDate;
    private String carrierCode;
    private String serviceCode;
    private String fulfillmentProviderCode;
    private String fulfillmentServiceCode;
    private Double shipmentCost;
    private Double insuranceCost;
    private Double fulfillmentFee;
    private Boolean voidRequested;
    private Boolean voided;
    private Boolean marketplaceNotified;
    private String notifyErrorMessage;
    private Address shipTo;
}
