package com.shipstation.api.rest.client;

import com.shipstation.api.rest.model.OrdersList;
import com.shipstation.api.rest.model.ShipStationOrder;
import com.shipstation.api.rest.model.ShipmentsList;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.PostExchange;

import java.util.Map;

public interface ShipStationHttpClient {
    @GetExchange("/orders")
    OrdersList getOrders(@RequestParam Map<String, Object> params);

    @GetExchange("/orders/{orderId}")
    ShipStationOrder getOrder(@PathVariable("orderId") Long orderId);

    @PostExchange("/orders/createorder")
    ShipStationOrder createOrUpdateOrder(@RequestBody ShipStationOrder order);

    @GetExchange("/shipments")
    ShipmentsList getShipments(@RequestParam Map<String, Object> params);
}
