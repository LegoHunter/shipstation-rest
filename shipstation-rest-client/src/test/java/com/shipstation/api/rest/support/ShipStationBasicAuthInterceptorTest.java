package com.shipstation.api.rest.support;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.http.client.MockClientHttpRequest;
import org.springframework.mock.http.client.MockClientHttpResponse;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

class ShipStationBasicAuthInterceptorTest {
    @Test
    void authorizationHeaderUsesBasicAuthToken() {
        ShipStationBasicAuthInterceptor interceptor = new ShipStationBasicAuthInterceptor("key", "secret");
        String expected = "Basic " + Base64.getEncoder().encodeToString("key:secret".getBytes(StandardCharsets.UTF_8));

        assertThat(interceptor.authorizationHeader()).isEqualTo(expected);
    }

    @Test
    void rejectsMissingCredentials() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> new ShipStationBasicAuthInterceptor(null, "secret"))
                .withMessage("apiKey is required");
        assertThatIllegalArgumentException()
                .isThrownBy(() -> new ShipStationBasicAuthInterceptor("key", " "))
                .withMessage("apiSecret is required");
    }

    @Test
    void interceptAddsAuthorizationHeaderAndExecutesRequest() throws Exception {
        ShipStationBasicAuthInterceptor interceptor = new ShipStationBasicAuthInterceptor("key", "secret");
        MockClientHttpRequest request = new MockClientHttpRequest();
        MockClientHttpResponse response = new MockClientHttpResponse(new byte[0], 200);

        MockClientHttpResponse actual = (MockClientHttpResponse) interceptor.intercept(
                request,
                new byte[0],
                (interceptedRequest, body) -> {
                    assertThat(interceptedRequest.getHeaders().getFirst(HttpHeaders.AUTHORIZATION))
                            .isEqualTo(interceptor.authorizationHeader());
                    assertThat(body).isEmpty();
                    return response;
                }
        );

        assertThat(actual).isSameAs(response);
    }
}