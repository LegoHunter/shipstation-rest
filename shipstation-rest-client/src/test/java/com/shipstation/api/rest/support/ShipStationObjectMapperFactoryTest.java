package com.shipstation.api.rest.support;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
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
    void createDeserializesAdvancedOptionsMergedIdsArray() throws Exception {
        ObjectMapper mapper = ShipStationObjectMapperFactory.create();

        ShipStationOrder order = mapper.readValue("""
                {
                  "orderNumber": "BL-123",
                  "advancedOptions": {
                    "mergedIds": [101, 102]
                  }
                }
                """, ShipStationOrder.class);

        assertThat(order.getAdvancedOptions().getMergedIds()).containsExactly(101L, 102L);
    }

    @Test
    void createDeserializesShipStationDateTimeWithoutOffsetAsUtc() throws Exception {
        ObjectMapper mapper = ShipStationObjectMapperFactory.create();

        ShipStationOrder order = mapper.readValue("""
                {
                  "orderDate": "2026-04-01T04:39:02.8300000",
                  "orderNumber": "BL-123"
                }
                """, ShipStationOrder.class);

        assertThat(order.getOrderDate()).isEqualTo(OffsetDateTime.parse("2026-04-01T04:39:02.830Z"));
    }

    @Test
    void createDeserializesOffsetDateTimeWithoutChangingOffset() throws Exception {
        ObjectMapper mapper = ShipStationObjectMapperFactory.create();

        ShipStationOrder order = mapper.readValue("""
                {
                  "orderDate": "2026-04-01T04:39:02.8300000-04:00",
                  "orderNumber": "BL-123"
                }
                """, ShipStationOrder.class);

        assertThat(order.getOrderDate()).isEqualTo(OffsetDateTime.parse("2026-04-01T04:39:02.830-04:00"));
    }

    @Test
    void createDeserializesOffsetDateTimeWithoutColonInOffset() throws Exception {
        ObjectMapper mapper = ShipStationObjectMapperFactory.create();

        ShipStationOrder order = mapper.readValue("""
                {
                  "shipDate": "2026-04-01T04:39:02.8300000-0400",
                  "orderNumber": "BL-123"
                }
                """, ShipStationOrder.class);

        assertThat(order.getShipDate()).isEqualTo(OffsetDateTime.parse("2026-04-01T04:39:02.830-04:00"));
    }

    @Test
    void createDeserializesMicrosoftJsonDateAsUtc() throws Exception {
        ObjectMapper mapper = ShipStationObjectMapperFactory.create();

        ShipStationOrder order = mapper.readValue("""
                {
                  "shipDate": "/Date(1775018342830-0400)/",
                  "orderNumber": "BL-123"
                }
                """, ShipStationOrder.class);

        assertThat(order.getShipDate()).isEqualTo(OffsetDateTime.parse("2026-04-01T04:39:02.830Z"));
    }

    @Test
    void createDeserializesShipStationDateOnlyAsStartOfDayUtc() throws Exception {
        ObjectMapper mapper = ShipStationObjectMapperFactory.create();

        ShipStationOrder order = mapper.readValue("""
                {
                  "shipDate": "2026-04-13",
                  "orderNumber": "BL-123"
                }
                """, ShipStationOrder.class);

        assertThat(order.getShipDate()).isEqualTo(OffsetDateTime.parse("2026-04-13T00:00:00Z"));
    }

    @Test
    void createDeserializesBlankOffsetDateTimeAsNull() throws Exception {
        ObjectMapper mapper = ShipStationObjectMapperFactory.create();

        ShipStationOrder order = mapper.readValue("""
                {
                  "orderDate": "",
                  "orderNumber": "BL-123"
                }
                """, ShipStationOrder.class);

        assertThat(order.getOrderDate()).isNull();
    }

    @Test
    void createDeserializesLiteralNullOffsetDateTimeAsNull() throws Exception {
        ObjectMapper mapper = ShipStationObjectMapperFactory.create();

        ShipStationOrder order = mapper.readValue("""
                {
                  "orderDate": "null",
                  "orderNumber": "BL-123"
                }
                """, ShipStationOrder.class);

        assertThat(order.getOrderDate()).isNull();
    }

    @Test
    void createRejectsInvalidOffsetDateTime() {
        ObjectMapper mapper = ShipStationObjectMapperFactory.create();

        assertThatThrownBy(() -> mapper.readValue("""
                {
                  "orderDate": "not-a-date",
                  "orderNumber": "BL-123"
                }
                """, ShipStationOrder.class))
                .isInstanceOf(InvalidFormatException.class)
                .hasMessageContaining("Expected an ISO offset date-time, a ShipStation local date-time, a ShipStation local date, or a Microsoft JSON date: not-a-date");
    }

    @Test
    void defaultMapperStillRejectsUnknownPropertiesToProveFactoryConfiguration() {
        ObjectMapper mapper = new ObjectMapper();

        assertThatThrownBy(() -> mapper.readValue("{\"message\":\"bad\",\"future\":true}", ShipStationError.class))
                .isInstanceOf(UnrecognizedPropertyException.class);
    }
}
