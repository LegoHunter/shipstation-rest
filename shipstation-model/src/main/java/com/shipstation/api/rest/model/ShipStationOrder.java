package com.shipstation.api.rest.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShipStationOrder {
    @Builder.Default
    private List<OrderItem> shipStationOrderItems = new ArrayList<>();
    private Long orderId;
    private String orderNumber;
    private String orderKey;
    private OffsetDateTime orderDate;
    private OffsetDateTime createDate;
    private OffsetDateTime modifyDate;
    private OffsetDateTime paymentDate;
    private OffsetDateTime shipByDate;
    private String orderStatus;
    private Long customerId;
    private String customerUsername;
    private String customerEmail;
    private Address billTo;
    private Address shipTo;
    private OrderItem[] items;
    private Double orderTotal;
    private Double amountPaid;
    private Double taxAmount;
    private Double shippingAmount;
    private String customerNotes;
    private String internalNotes;
    private Boolean gift;
    private String giftMessage;
    private String paymentMethod;
    private String requestedShippingService;
    private String carrierCode;
    private String serviceCode;
    private String packageCode;
    private String confirmation;
    private OffsetDateTime shipDate;
    private OffsetDateTime holdUntilDate;
    private Weight weight;
    private Dimensions dimensions;
    private InsuranceOptions insuranceOptions;
    private InternationalOptions internationalOptions;
    private AdvancedOptions advancedOptions;
    private Integer[] tagIds;
    private String userId;
    private Boolean externallyFulfilled;
    private String externallyFulfilledBy;

    public OrderItem[] getItems() {
        if (items != null) {
            return items;
        }
        return shipStationOrderItems.toArray(new OrderItem[0]);
    }

    public boolean isPaid() {
        return statusRank(orderStatus) >= statusRank(OrderStatus.AWAITING_SHIPMENT.label());
    }

    public boolean isShipped() {
        return statusRank(orderStatus) >= statusRank(OrderStatus.SHIPPED.label());
    }

    public void updateStatusToPaid(OffsetDateTime paidAt, Double paidAmount) {
        if (!isPaid()) {
            paymentDate = paymentDate == null ? paidAt : paymentDate;
            orderStatus = OrderStatus.AWAITING_SHIPMENT.label();
            amountPaid = paidAmount;
        }
    }

    private static int statusRank(String status) {
        if (status == null || status.isBlank()) {
            return statusRank(OrderStatus.NONE.label());
        }
        return switch (status.toLowerCase(Locale.ROOT)) {
            case "awaiting_payment" -> 1;
            case "awaiting_shipment" -> 2;
            case "shipped" -> 3;
            case "on_hold" -> 2;
            case "cancelled" -> 3;
            default -> 0;
        };
    }
}
