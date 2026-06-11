package com.shipstation.api.rest.support;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Slf4j
@RequiredArgsConstructor
public class ShipStationHttpLoggingInterceptor implements ClientHttpRequestInterceptor {
    private static final Set<String> SENSITIVE_HEADERS = Set.of(
            HttpHeaders.AUTHORIZATION.toLowerCase(),
            HttpHeaders.COOKIE.toLowerCase(),
            HttpHeaders.SET_COOKIE.toLowerCase()
    );

    private final ShipStationHttpLoggingProperties properties;

    @Override
    public ClientHttpResponse intercept(
            HttpRequest request,
            byte[] body,
            ClientHttpRequestExecution execution
    ) throws IOException {
        if (!properties.isEnabled() || !log.isDebugEnabled()) {
            return execution.execute(request, body);
        }
        logRequest(request, body);
        ClientHttpResponse response = execution.execute(request, body);
        logResponse(request, response);
        return response;
    }

    Object headers(HttpHeaders headers) {
        if (!properties.isIncludeHeaders()) {
            return "[disabled]";
        }
        HttpHeaders redacted = new HttpHeaders();
        for (Map.Entry<String, List<String>> entry : headers.entrySet()) {
            if (SENSITIVE_HEADERS.contains(entry.getKey().toLowerCase())) {
                redacted.put(entry.getKey(), List.of("[redacted]"));
            } else {
                redacted.put(entry.getKey(), entry.getValue());
            }
        }
        return redacted;
    }

    Object body(byte[] body) {
        if (!properties.isIncludeBody()) {
            return "[disabled]";
        }
        if (body == null || body.length == 0) {
            return "";
        }
        String value = new String(body, StandardCharsets.UTF_8);
        int maxBodyLength = properties.getMaxBodyLength();
        if (maxBodyLength < 0 || value.length() <= maxBodyLength) {
            return value;
        }
        return value.substring(0, maxBodyLength) + "...[truncated]";
    }

    private void logRequest(HttpRequest request, byte[] body) {
        log.debug(
                "shipstation.rest.request method={} uri={} headers={} body={}",
                request.getMethod(),
                request.getURI(),
                headers(request.getHeaders()),
                body(body)
        );
    }

    private void logResponse(HttpRequest request, ClientHttpResponse response) throws IOException {
        log.debug(
                "shipstation.rest.response method={} uri={} statusCode={} statusText={} headers={} body={}",
                request.getMethod(),
                request.getURI(),
                response.getStatusCode().value(),
                response.getStatusText(),
                headers(response.getHeaders()),
                body(StreamUtils.copyToByteArray(response.getBody()))
        );
    }
}
