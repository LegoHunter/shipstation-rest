package com.shipstation.api.rest.autoconfigure;

import com.shipstation.api.rest.support.ShipStationHttpLoggingProperties;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.net.URI;

@Data
@ConfigurationProperties(prefix = "shipstation.rest")
public class ShipStationRestProperties {
    private URI uri = URI.create("https://ssapi.shipstation.com");
    private String apiKey;
    private String apiSecret;
    private ShipStationHttpLoggingProperties httpLogging = new ShipStationHttpLoggingProperties();
}
