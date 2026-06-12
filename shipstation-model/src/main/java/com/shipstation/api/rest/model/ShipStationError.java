package com.shipstation.api.rest.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShipStationError {
    private String message;
    private String exceptionMessage;
    private String exceptionType;
    private String stackTrace;
    private String deserializationErrorMessage;
}
