package com.shipstation.api.rest.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Address {
    private String name;
    private String company;
    private String street1;
    private String street2;
    private String street3;
    private String city;
    private String state;
    private String postalCode;
    private String country;
    private String phone;
    private Boolean residential;
}
