package com.shipstation.api.rest.client;

import com.shipstation.api.rest.model.OrdersList;
import com.shipstation.api.rest.model.ShipStationOrder;
import com.shipstation.api.rest.model.ShipmentsList;

import java.util.Map;

public interface ShipStationRestClient {
    OrdersList getOrders();

    OrdersList getOrders(Map<String, Object> params);

    ShipStationOrder getOrder(Long orderId);

    ShipStationOrder createOrUpdateOrder(ShipStationOrder order);

    ShipmentsList getShipments();

    ShipmentsList getShipments(Map<String, Object> params);
}
