package com.shipstation.api.rest.support;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class ShipStationBasicAuthInterceptor implements ClientHttpRequestInterceptor {
    private final String apiKey;
    private final String apiSecret;

    public ShipStationBasicAuthInterceptor(String apiKey, String apiSecret) {
        this.apiKey = requireText(apiKey, "apiKey");
        this.apiSecret = requireText(apiSecret, "apiSecret");
    }

    @Override
    public ClientHttpResponse intercept(
            HttpRequest request,
            byte[] body,
            ClientHttpRequestExecution execution
    ) throws IOException {
        request.getHeaders().set(HttpHeaders.AUTHORIZATION, authorizationHeader());
        return execution.execute(request, body);
    }

    public String authorizationHeader() {
        String token = Base64.getEncoder().encodeToString((apiKey + ":" + apiSecret).getBytes(StandardCharsets.UTF_8));
        return "Basic " + token;
    }

    private static String requireText(String value, String name) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(name + " is required");
        }
        return value;
    }
}
