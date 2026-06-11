package com.shipstation.api.rest.support;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;
import com.shipstation.api.rest.model.ShipStationError;
import com.shipstation.api.rest.model.ShipStationOrder;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ShipStationObjectMapperFactoryTest {
    @Test
    void createSerializesOffsetDatesAsIsoStringsAndSkipsNulls() throws Exception {
        ObjectMapper mapper = ShipStationObjectMapperFactory.create();
        ShipStationOrder order = ShipStationOrder.builder()
                .orderDate(OffsetDateTime.parse("2026-06-10T12:00:00Z"))
                .orderNumber("BL-123")
                .build();

        String json = mapper.writeValueAsString(order);

        assertThat(json).contains("\"orderDate\":\"2026-06-10T12:00:00Z\"");
        assertThat(json).contains("\"orderNumber\":\"BL-123\"");
        assertThat(json).doesNotContain("orderId");
    }

    @Test
    void createIgnoresUnknownProperties() throws Exception {
        ObjectMapper mapper = ShipStationObjectMapperFactory.create();

        assertThatCode(() -> mapper.readValue("{\"message\":\"bad\",\"future\":true}", ShipStationError.class))
                .doesNotThrowAnyException();
    }

    @Test
    void defaultMapperStillRejectsUnknownPropertiesToProveFactoryConfiguration() {
        ObjectMapper mapper = new ObjectMapper();

        assertThatThrownBy(() -> mapper.readValue("{\"message\":\"bad\",\"future\":true}", ShipStationError.class))
                .isInstanceOf(UnrecognizedPropertyException.class);
    }
}