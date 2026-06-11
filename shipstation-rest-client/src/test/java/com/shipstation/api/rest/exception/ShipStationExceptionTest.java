package com.shipstation.api.rest.exception;

import com.shipstation.api.rest.model.ShipStationError;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ShipStationExceptionTest {
    @Test
    void clientExceptionUsesMessageWhenPresent() {
        ShipStationError error = ShipStationError.builder().message("bad request").build();
        ShipStationClientException exception = new ShipStationClientException(400, "GET /orders", error);

        assertThat(exception.getStatusCode()).isEqualTo(400);
        assertThat(exception.getRequest()).isEqualTo("GET /orders");
        assertThat(exception.getShipStationError()).isSameAs(error);
        assertThat(exception).hasMessage("ShipStation client error status [400] request [GET /orders] message [bad request]");
    }

    @Test
    void clientExceptionFallsBackToExceptionMessage() {
        ShipStationError error = ShipStationError.builder().exceptionMessage("invalid").build();
        ShipStationClientException exception = new ShipStationClientException(422, "POST /orders", error);

        assertThat(exception).hasMessage("ShipStation client error status [422] request [POST /orders] message [invalid]");
    }

    @Test
    void clientExceptionHandlesNullError() {
        ShipStationClientException exception = new ShipStationClientException(404, "GET /orders/1", null);

        assertThat(exception.getShipStationError()).isNull();
        assertThat(exception).hasMessage("ShipStation client error status [404] request [GET /orders/1] message [null]");
    }

    @Test
    void serverExceptionExposesStatusAndError() {
        ShipStationError error = ShipStationError.builder().message("server error").build();
        ShipStationServerException exception = new ShipStationServerException(500, error);

        assertThat(exception.getStatusCode()).isEqualTo(500);
        assertThat(exception.getShipStationError()).isSameAs(error);
        assertThat(exception).hasMessage("ShipStation server error status [500] message [server error]");
    }

    @Test
    void serverExceptionHandlesNullError() {
        ShipStationServerException exception = new ShipStationServerException(503, null);

        assertThat(exception.getShipStationError()).isNull();
        assertThat(exception).hasMessage("ShipStation server error status [503] message [null]");
    }
}