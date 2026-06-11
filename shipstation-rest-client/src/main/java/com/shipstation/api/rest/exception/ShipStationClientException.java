package com.shipstation.api.rest.exception;

import com.shipstation.api.rest.model.ShipStationError;

public class ShipStationClientException extends RuntimeException {
    private final int statusCode;
    private final String request;
    private final ShipStationError shipStationError;

    public ShipStationClientException(int statusCode, String request, ShipStationError shipStationError) {
        super("ShipStation client error status [%d] request [%s] message [%s]".formatted(
                statusCode,
                request,
                errorMessage(shipStationError)
        ));
        this.statusCode = statusCode;
        this.request = request;
        this.shipStationError = shipStationError;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getRequest() {
        return request;
    }

    public ShipStationError getShipStationError() {
        return shipStationError;
    }

    private static String errorMessage(ShipStationError shipStationError) {
        if (shipStationError == null) {
            return null;
        }
        if (shipStationError.getMessage() != null) {
            return shipStationError.getMessage();
        }
        return shipStationError.getExceptionMessage();
    }
}
