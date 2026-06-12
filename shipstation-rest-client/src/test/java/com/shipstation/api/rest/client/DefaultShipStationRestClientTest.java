package com.shipstation.api.rest.client;

import com.shipstation.api.rest.model.OrdersList;
import com.shipstation.api.rest.model.ShipStationOrder;
import com.shipstation.api.rest.model.ShipmentsList;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DefaultShipStationRestClientTest {
    private final ShipStationHttpClient httpClient = mock(ShipStationHttpClient.class);
    private final DefaultShipStationRestClient client = new DefaultShipStationRestClient(httpClient);

    @Test
    void getOrdersUsesEmptyParamsWhenNoneAreSupplied() {
        OrdersList expected = OrdersList.builder().total(1).build();
        when(httpClient.getOrders(Map.of())).thenReturn(expected);

        OrdersList actual = client.getOrders();

        assertThat(actual).isSameAs(expected);
        verify(httpClient).getOrders(Map.of());
    }

    @Test
    void getOrdersCopiesProvidedParamsAndTreatsNullAsEmpty() {
        client.getOrders(null);
        verify(httpClient).getOrders(Map.of());

        Map<String, Object> params = Map.of("orderNumber", "BL-123");
        client.getOrders(params);

        ArgumentCaptor<Map<String, Object>> captor = ArgumentCaptor.forClass(Map.class);
        verify(httpClient, times(2)).getOrders(captor.capture());
        assertThat(captor.getAllValues().get(1)).containsEntry("orderNumber", "BL-123");
        assertThat(captor.getAllValues().get(1)).isNotSameAs(params);
    }

    @Test
    void delegatesOrderLookupAndCreateOrUpdate() {
        ShipStationOrder order = ShipStationOrder.builder().orderId(42L).build();
        when(httpClient.getOrder(42L)).thenReturn(order);
        when(httpClient.createOrUpdateOrder(order)).thenReturn(order);

        assertThat(client.getOrder(42L)).isSameAs(order);
        assertThat(client.createOrUpdateOrder(order)).isSameAs(order);
    }

    @Test
    void getShipmentsUsesEmptyOrCopiedParams() {
        ShipmentsList expected = ShipmentsList.builder().total(1).build();
        when(httpClient.getShipments(Map.of())).thenReturn(expected);

        assertThat(client.getShipments()).isSameAs(expected);
        client.getShipments(Map.of("orderId", 42L));

        verify(httpClient).getShipments(Map.of());
        verify(httpClient).getShipments(Map.of("orderId", 42L));
    }
}
