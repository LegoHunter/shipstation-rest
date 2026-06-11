package com.shipstation.api.rest.client;

import com.shipstation.api.rest.model.OrdersList;
import com.shipstation.api.rest.model.ShipStationOrder;
import com.shipstation.api.rest.model.ShipmentsList;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor
public class DefaultShipStationRestClient implements ShipStationRestClient {
    private final ShipStationHttpClient shipStationHttpClient;

    @Override
    public OrdersList getOrders() {
        return getOrders(Map.of());
    }

    @Override
    public OrdersList getOrders(Map<String, Object> params) {
        return shipStationHttpClient.getOrders(nonNullParams(params));
    }

    @Override
    public ShipStationOrder getOrder(Long orderId) {
        return shipStationHttpClient.getOrder(orderId);
    }

    @Override
    public ShipStationOrder createOrUpdateOrder(ShipStationOrder order) {
        return shipStationHttpClient.createOrUpdateOrder(order);
    }

    @Override
    public ShipmentsList getShipments() {
        return getShipments(Map.of());
    }

    @Override
    public ShipmentsList getShipments(Map<String, Object> params) {
        return shipStationHttpClient.getShipments(nonNullParams(params));
    }

    private Map<String, Object> nonNullParams(Map<String, Object> params) {
        return params == null ? new HashMap<>() : new HashMap<>(params);
    }
}
