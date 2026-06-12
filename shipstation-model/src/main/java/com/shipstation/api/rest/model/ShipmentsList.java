package com.shipstation.api.rest.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShipmentsList {
    private List<Shipment> shipments;
    private Integer total;
    private Integer page;
    private Integer pages;
}
