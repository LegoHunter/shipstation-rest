package com.shipstation.api.rest.autoconfigure;

import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import com.shipstation.api.rest.client.ShipStationRestClient;
import com.shipstation.api.rest.exception.ShipStationClientException;
import com.shipstation.api.rest.exception.ShipStationServerException;
import com.shipstation.api.rest.model.OrdersList;
import com.shipstation.api.rest.model.ShipStationOrder;
import com.shipstation.api.rest.model.ShipmentsList;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.http.HttpHeaders;

import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.badRequest;
import static com.github.tomakehurst.wiremock.client.WireMock.containing;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.noContent;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.serverError;
import static com.github.tomakehurst.wiremock.client.WireMock.temporaryRedirect;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ShipStationRestClientIntegrationTest {
    private static final String AUTHORIZATION = "Basic a2V5OnNlY3JldA==";

    @RegisterExtension
    static WireMockExtension wireMock = WireMockExtension.newInstance()
            .options(wireMockConfig().dynamicPort())
            .build();

    private ApplicationContextRunner contextRunner() {
        return new ApplicationContextRunner()
                .withConfiguration(AutoConfigurations.of(ShipStationRestAutoConfiguration.class))
                .withPropertyValues(
                        "shipstation.rest.uri=" + wireMock.baseUrl(),
                        "shipstation.rest.api-key=key",
                        "shipstation.rest.api-secret=secret"
                );
    }

    @Test
    void getOrdersSendsQueryParamsAndBasicAuth() {
        wireMock.stubFor(get(urlPathEqualTo("/orders"))
                .withHeader(HttpHeaders.AUTHORIZATION, equalTo(AUTHORIZATION))
                .withQueryParam("orderNumber", equalTo("BL-123"))
                .willReturn(okJson("""
                        {
                          "orders": [
                            {
                              "orderId": 42,
                              "orderNumber": "BL-123",
                              "orderStatus": "awaiting_shipment",
                              "orderDate": "2026-06-10T12:00:00Z",
                              "items": [{"orderItemId": 7, "sku": "3001-RED", "quantity": 2}]
                            }
                          ],
                          "total": 1,
                          "page": 1,
                          "pages": 1
                        }
                        """)));

        contextRunner().run(context -> {
            OrdersList orders = context.getBean(ShipStationRestClient.class)
                    .getOrders(Map.of("orderNumber", "BL-123"));

            assertThat(orders.getTotal()).isEqualTo(1);
            assertThat(orders.getOrders()).hasSize(1);
            ShipStationOrder order = orders.getOrders().getFirst();
            assertThat(order.getOrderId()).isEqualTo(42L);
            assertThat(order.getOrderNumber()).isEqualTo("BL-123");
            assertThat(order.getItems()).hasSize(1);
            assertThat(order.getItems()[0].getSku()).isEqualTo("3001-RED");
        });
    }

    @Test
    void getOrderFetchesSingleOrder() {
        wireMock.stubFor(get(urlEqualTo("/orders/42"))
                .withHeader(HttpHeaders.AUTHORIZATION, equalTo(AUTHORIZATION))
                .willReturn(okJson("{\"orderId\":42,\"orderNumber\":\"BL-123\"}")));

        contextRunner().run(context -> {
            ShipStationOrder order = context.getBean(ShipStationRestClient.class).getOrder(42L);

            assertThat(order.getOrderId()).isEqualTo(42L);
            assertThat(order.getOrderNumber()).isEqualTo("BL-123");
        });
    }

    @Test
    void createOrUpdateOrderPostsPayload() {
        wireMock.stubFor(post(urlEqualTo("/orders/createorder"))
                .withHeader(HttpHeaders.AUTHORIZATION, equalTo(AUTHORIZATION))
                .withRequestBody(containing("\"orderNumber\":\"BL-123\""))
                .willReturn(okJson("{\"orderId\":42,\"orderNumber\":\"BL-123\"}")));

        contextRunner().run(context -> {
            ShipStationOrder order = context.getBean(ShipStationRestClient.class)
                    .createOrUpdateOrder(ShipStationOrder.builder().orderNumber("BL-123").build());

            assertThat(order.getOrderId()).isEqualTo(42L);
            assertThat(order.getOrderNumber()).isEqualTo("BL-123");
        });
    }

    @Test
    void getShipmentsSendsQueryParams() {
        wireMock.stubFor(get(urlPathEqualTo("/shipments"))
                .withHeader(HttpHeaders.AUTHORIZATION, equalTo(AUTHORIZATION))
                .withQueryParam("orderId", equalTo("42"))
                .willReturn(okJson("""
                        {
                          "shipments": [{"shipmentId": 99, "orderId": 42, "trackingNumber": "1Z999"}],
                          "total": 1,
                          "page": 1,
                          "pages": 1
                        }
                        """)));

        contextRunner().run(context -> {
            ShipmentsList shipments = context.getBean(ShipStationRestClient.class)
                    .getShipments(Map.of("orderId", 42));

            assertThat(shipments.getTotal()).isEqualTo(1);
            assertThat(shipments.getShipments().getFirst().getTrackingNumber()).isEqualTo("1Z999");
        });
    }

    @Test
    void mapsClientErrorsWithJsonBody() {
        wireMock.stubFor(get(urlEqualTo("/orders/400"))
                .willReturn(badRequest().withHeader(HttpHeaders.CONTENT_TYPE, "application/json")
                        .withBody("{\"message\":\"Invalid request\"}")));

        contextRunner().run(context -> assertThatThrownBy(() -> context.getBean(ShipStationRestClient.class).getOrder(400L))
                .isInstanceOfSatisfying(ShipStationClientException.class, exception -> {
                    assertThat(exception.getStatusCode()).isEqualTo(400);
                    assertThat(exception.getRequest()).contains("GET", "/orders/400");
                    assertThat(exception.getShipStationError().getMessage()).isEqualTo("Invalid request");
                }));
    }

    @Test
    void mapsClientErrorsWithEmptyBody() {
        wireMock.stubFor(get(urlEqualTo("/orders/404"))
                .willReturn(noContent().withStatus(404).withStatusMessage("Not Found")));

        contextRunner().run(context -> assertThatThrownBy(() -> context.getBean(ShipStationRestClient.class).getOrder(404L))
                .isInstanceOfSatisfying(ShipStationClientException.class, exception -> {
                    assertThat(exception.getStatusCode()).isEqualTo(404);
                    assertThat(exception.getShipStationError().getMessage()).isEqualTo("Not Found");
                }));
    }

    @Test
    void mapsClientErrorsWithUnparseableBody() {
        wireMock.stubFor(get(urlEqualTo("/orders/422"))
                .willReturn(badRequest().withStatus(422).withBody("not-json")));

        contextRunner().run(context -> assertThatThrownBy(() -> context.getBean(ShipStationRestClient.class).getOrder(422L))
                .isInstanceOfSatisfying(ShipStationClientException.class, exception -> {
                    assertThat(exception.getStatusCode()).isEqualTo(422);
                    assertThat(exception.getShipStationError().getMessage()).isEqualTo("not-json");
                    assertThat(exception.getShipStationError().getDeserializationErrorMessage()).isNotBlank();
                }));
    }

    @Test
    void mapsRedirectsAsClientErrors() {
        wireMock.stubFor(post(urlEqualTo("/orders/createorder"))
                .willReturn(temporaryRedirect("/login")));

        contextRunner().run(context -> assertThatThrownBy(() -> context.getBean(ShipStationRestClient.class)
                .createOrUpdateOrder(ShipStationOrder.builder().orderNumber("BL-123").build()))
                .isInstanceOfSatisfying(ShipStationClientException.class, exception -> {
                    assertThat(exception.getStatusCode()).isEqualTo(302);
                    assertThat(exception.getShipStationError().getMessage()).contains("/login");
                }));
    }

    @Test
    void mapsServerErrors() {
        wireMock.stubFor(get(urlEqualTo("/orders/500"))
                .willReturn(serverError().withHeader(HttpHeaders.CONTENT_TYPE, "application/json")
                        .withBody("{\"message\":\"ShipStation is unavailable\"}")));

        contextRunner().run(context -> assertThatThrownBy(() -> context.getBean(ShipStationRestClient.class).getOrder(500L))
                .isInstanceOfSatisfying(ShipStationServerException.class, exception -> {
                    assertThat(exception.getStatusCode()).isEqualTo(500);
                    assertThat(exception.getShipStationError().getMessage()).isEqualTo("ShipStation is unavailable");
                }));
    }
}
