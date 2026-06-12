package com.shipstation.api.rest.support;

import lombok.Data;

@Data
public class ShipStationHttpLoggingProperties {
    private boolean enabled = false;
    private boolean includeHeaders = false;
    private boolean includeBody = true;
    private int maxBodyLength = -1;
}
