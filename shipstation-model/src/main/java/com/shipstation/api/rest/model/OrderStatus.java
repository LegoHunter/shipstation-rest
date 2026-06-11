package com.shipstation.api.rest.model;

public enum OrderStatus {
    NONE("none"),
    AWAITING_PAYMENT("awaiting_payment"),
    AWAITING_SHIPMENT("awaiting_shipment"),
    SHIPPED("shipped"),
    ON_HOLD("on_hold"),
    CANCELLED("cancelled");

    private final String label;

    OrderStatus(String label) {
        this.label = label;
    }

    public String label() {
        return label;
    }
}
