package com.shipstation.api.rest.model;

import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ShipStationOrderTest {
    @Test
    void getItemsReturnsExplicitItemsWhenPresent() {
        OrderItem explicit = OrderItem.builder().lineItemKey("explicit").build();
        OrderItem fallback = OrderItem.builder().lineItemKey("fallback").build();
        ShipStationOrder order = ShipStationOrder.builder()
                .items(new OrderItem[]{explicit})
                .shipStationOrderItems(List.of(fallback))
                .build();

        assertThat(order.getItems()).containsExactly(explicit);
    }

    @Test
    void getItemsReturnsMutableItemListWhenExplicitItemsAreAbsent() {
        OrderItem item = OrderItem.builder().lineItemKey("item-1").build();
        ShipStationOrder order = ShipStationOrder.builder()
                .shipStationOrderItems(List.of(item))
                .build();

        assertThat(order.getItems()).containsExactly(item);
    }

    @Test
    void statusHelpersClassifyPaidAndShippedStates() {
        assertThat(ShipStationOrder.builder().orderStatus(null).build().isPaid()).isFalse();
        assertThat(ShipStationOrder.builder().orderStatus(OrderStatus.AWAITING_PAYMENT.label()).build().isPaid()).isFalse();
        assertThat(ShipStationOrder.builder().orderStatus(OrderStatus.AWAITING_SHIPMENT.label()).build().isPaid()).isTrue();
        assertThat(ShipStationOrder.builder().orderStatus(OrderStatus.ON_HOLD.label()).build().isPaid()).isTrue();
        assertThat(ShipStationOrder.builder().orderStatus(OrderStatus.SHIPPED.label()).build().isShipped()).isTrue();
        assertThat(ShipStationOrder.builder().orderStatus(OrderStatus.CANCELLED.label()).build().isShipped()).isTrue();
    }

    @Test
    void updateStatusToPaidSetsAwaitingShipmentWhenOrderIsNotAlreadyPaid() {
        OffsetDateTime paidAt = OffsetDateTime.parse("2026-06-10T12:00:00Z");
        ShipStationOrder order = ShipStationOrder.builder()
                .orderStatus(OrderStatus.AWAITING_PAYMENT.label())
                .build();

        order.updateStatusToPaid(paidAt, 14.50);

        assertThat(order.getOrderStatus()).isEqualTo(OrderStatus.AWAITING_SHIPMENT.label());
        assertThat(order.getPaymentDate()).isEqualTo(paidAt);
        assertThat(order.getAmountPaid()).isEqualTo(14.50);
    }

    @Test
    void updateStatusToPaidDoesNotDowngradeAlreadyPaidOrders() {
        OffsetDateTime originalPaidAt = OffsetDateTime.parse("2026-06-09T12:00:00Z");
        ShipStationOrder order = ShipStationOrder.builder()
                .orderStatus(OrderStatus.SHIPPED.label())
                .paymentDate(originalPaidAt)
                .amountPaid(99.00)
                .build();

        order.updateStatusToPaid(OffsetDateTime.parse("2026-06-10T12:00:00Z"), 14.50);

        assertThat(order.getOrderStatus()).isEqualTo(OrderStatus.SHIPPED.label());
        assertThat(order.getPaymentDate()).isEqualTo(originalPaidAt);
        assertThat(order.getAmountPaid()).isEqualTo(99.00);
    }

    @Test
    void orderStatusLabelsMatchShipStationApiValues() {
        assertThat(OrderStatus.NONE.label()).isEqualTo("none");
        assertThat(OrderStatus.AWAITING_PAYMENT.label()).isEqualTo("awaiting_payment");
        assertThat(OrderStatus.AWAITING_SHIPMENT.label()).isEqualTo("awaiting_shipment");
        assertThat(OrderStatus.SHIPPED.label()).isEqualTo("shipped");
        assertThat(OrderStatus.ON_HOLD.label()).isEqualTo("on_hold");
        assertThat(OrderStatus.CANCELLED.label()).isEqualTo("cancelled");
    }
}