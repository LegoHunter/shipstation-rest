package com.shipstation.api.rest.support;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.mock.http.client.MockClientHttpRequest;
import org.springframework.mock.http.client.MockClientHttpResponse;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(OutputCaptureExtension.class)
class ShipStationHttpLoggingInterceptorTest {
    @Test
    void interceptDelegatesWithoutReadingResponseWhenDisabled() throws Exception {
        ShipStationHttpLoggingProperties properties = new ShipStationHttpLoggingProperties();
        properties.setEnabled(false);
        ShipStationHttpLoggingInterceptor interceptor = new ShipStationHttpLoggingInterceptor(properties);
        MockClientHttpRequest request = new MockClientHttpRequest();
        request.setURI(URI.create("https://ssapi.shipstation.com/orders"));
        MockClientHttpResponse response = new MockClientHttpResponse("ok".getBytes(StandardCharsets.UTF_8), 200);

        assertThat(interceptor.intercept(request, new byte[0], (interceptedRequest, body) -> response)).isSameAs(response);
    }

    @Test
    void headersCanBeDisabled() {
        ShipStationHttpLoggingProperties properties = new ShipStationHttpLoggingProperties();
        properties.setIncludeHeaders(false);
        ShipStationHttpLoggingInterceptor interceptor = new ShipStationHttpLoggingInterceptor(properties);

        assertThat(interceptor.headers(new HttpHeaders())).isEqualTo("[disabled]");
    }

    @Test
    void headersRedactSensitiveValuesWhenEnabled() {
        ShipStationHttpLoggingProperties properties = new ShipStationHttpLoggingProperties();
        properties.setIncludeHeaders(true);
        ShipStationHttpLoggingInterceptor interceptor = new ShipStationHttpLoggingInterceptor(properties);
        HttpHeaders headers = new HttpHeaders();
        headers.put(HttpHeaders.AUTHORIZATION, List.of("Basic abc"));
        headers.put(HttpHeaders.COOKIE, List.of("cookie=value"));
        headers.put(HttpHeaders.SET_COOKIE, List.of("set-cookie=value"));
        headers.put("X-Test", List.of("safe"));

        Object redacted = interceptor.headers(headers);

        assertThat(redacted).isInstanceOf(HttpHeaders.class);
        HttpHeaders actual = (HttpHeaders) redacted;
        assertThat(actual.get(HttpHeaders.AUTHORIZATION)).containsExactly("[redacted]");
        assertThat(actual.get(HttpHeaders.COOKIE)).containsExactly("[redacted]");
        assertThat(actual.get(HttpHeaders.SET_COOKIE)).containsExactly("[redacted]");
        assertThat(actual.get("X-Test")).containsExactly("safe");
    }

    @Test
    void bodyCanBeDisabledEmptyFullOrTruncated() {
        ShipStationHttpLoggingProperties properties = new ShipStationHttpLoggingProperties();
        ShipStationHttpLoggingInterceptor interceptor = new ShipStationHttpLoggingInterceptor(properties);

        properties.setIncludeBody(false);
        assertThat(interceptor.body("abc".getBytes(StandardCharsets.UTF_8))).isEqualTo("[disabled]");

        properties.setIncludeBody(true);
        assertThat(interceptor.body(null)).isEqualTo("");
        assertThat(interceptor.body(new byte[0])).isEqualTo("");
        assertThat(interceptor.body("abc".getBytes(StandardCharsets.UTF_8))).isEqualTo("abc");

        properties.setMaxBodyLength(2);
        assertThat(interceptor.body("abc".getBytes(StandardCharsets.UTF_8))).isEqualTo("ab...[truncated]");
    }

    @Test
    void interceptLogsRequestAndResponseWhenEnabledAndDebugIsEnabled(CapturedOutput output) throws Exception {
        Logger logger = (Logger) LoggerFactory.getLogger(ShipStationHttpLoggingInterceptor.class);
        Level originalLevel = logger.getLevel();
        logger.setLevel(Level.DEBUG);
        try {
            ShipStationHttpLoggingProperties properties = new ShipStationHttpLoggingProperties();
            properties.setEnabled(true);
            properties.setIncludeHeaders(true);
            ShipStationHttpLoggingInterceptor interceptor = new ShipStationHttpLoggingInterceptor(properties);
            MockClientHttpRequest request = new MockClientHttpRequest(HttpMethod.POST, URI.create("https://ssapi.shipstation.com/orders/createorder"));
            request.getHeaders().set(HttpHeaders.AUTHORIZATION, "Basic abc");
            MockClientHttpResponse response = new MockClientHttpResponse("response-body".getBytes(StandardCharsets.UTF_8), 200);
            response.getHeaders().set("X-Response", "safe");

            assertThat(interceptor.intercept(request, "request-body".getBytes(StandardCharsets.UTF_8), (interceptedRequest, body) -> response))
                    .isSameAs(response);

            assertThat(output.getOut())
                    .contains("shipstation.rest.request method=POST")
                    .contains("shipstation.rest.response method=POST")
                    .contains("body=request-body")
                    .contains("body=response-body")
                    .contains("[redacted]");
        } finally {
            logger.setLevel(originalLevel);
        }
    }
}
