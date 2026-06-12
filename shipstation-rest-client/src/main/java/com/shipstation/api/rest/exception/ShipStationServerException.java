package com.shipstation.api.rest.exception;

import com.shipstation.api.rest.model.ShipStationError;

public class ShipStationServerException extends RuntimeException {
    private final int statusCode;
    private final ShipStationError shipStationError;

    public ShipStationServerException(int statusCode, ShipStationError shipStationError) {
        super("ShipStation server error status [%d] message [%s]".formatted(
                statusCode,
                shipStationError == null ? null : shipStationError.getMessage()
        ));
        this.statusCode = statusCode;
        this.shipStationError = shipStationError;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public ShipStationError getShipStationError() {
        return shipStationError;
    }
}
