package com.shipstation.api.rest.autoconfigure;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shipstation.api.rest.client.ShipStationHttpClient;
import com.shipstation.api.rest.client.ShipStationRestClient;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.assertThat;

class ShipStationRestAutoConfigurationTest {
    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(ShipStationRestAutoConfiguration.class))
            .withPropertyValues(
                    "shipstation.rest.api-key=test-key",
                    "shipstation.rest.api-secret=test-secret"
            );

    @Test
    void autoConfiguresClientBeansWithDefaults() {
        contextRunner.run(context -> {
            assertThat(context).hasSingleBean(ShipStationRestProperties.class);
            assertThat(context).hasBean("shipStationRestObjectMapper");
            assertThat(context).hasBean("shipStationBasicAuthInterceptor");
            assertThat(context).hasBean("shipStationRestClientDelegate");
            assertThat(context).hasSingleBean(ShipStationHttpClient.class);
            assertThat(context).hasSingleBean(ShipStationRestClient.class);
            assertThat(context.getBean(ShipStationRestProperties.class).getUri().toString())
                    .isEqualTo("https://ssapi.shipstation.com");
        });
    }

    @Test
    void bindsHttpLoggingProperties() {
        contextRunner.withPropertyValues(
                "shipstation.rest.http-logging.enabled=true",
                "shipstation.rest.http-logging.include-headers=true",
                "shipstation.rest.http-logging.include-body=false",
                "shipstation.rest.http-logging.max-body-length=12"
        ).run(context -> {
            ShipStationRestProperties properties = context.getBean(ShipStationRestProperties.class);
            assertThat(properties.getHttpLogging().isEnabled()).isTrue();
            assertThat(properties.getHttpLogging().isIncludeHeaders()).isTrue();
            assertThat(properties.getHttpLogging().isIncludeBody()).isFalse();
            assertThat(properties.getHttpLogging().getMaxBodyLength()).isEqualTo(12);
        });
    }

    @Test
    void backsOffWhenClientBeansAreProvided() {
        contextRunner.withUserConfiguration(CustomClientConfiguration.class).run(context -> {
            assertThat(context).hasSingleBean(ShipStationRestClient.class);
            assertThat(context.getBean(ShipStationRestClient.class)).isSameAs(CustomClientConfiguration.CLIENT);
        });
    }

    @Test
    void failsFastWhenCredentialsAreMissing() {
        new ApplicationContextRunner()
                .withConfiguration(AutoConfigurations.of(ShipStationRestAutoConfiguration.class))
                .run(context -> {
                    assertThat(context).hasFailed();
                    assertThat(context.getStartupFailure()).hasRootCauseMessage("apiKey is required");
                });
    }

    @Test
    void backsOffWhenInfrastructureBeansAreProvided() {
        contextRunner.withUserConfiguration(CustomInfrastructureConfiguration.class).run(context -> {
            assertThat(context.getBean("shipStationRestObjectMapper")).isSameAs(CustomInfrastructureConfiguration.MAPPER);
            assertThat(context.getBean("shipStationRestClientDelegate")).isSameAs(CustomInfrastructureConfiguration.REST_CLIENT);
        });
    }

    @Configuration(proxyBeanMethods = false)
    static class CustomClientConfiguration {
        static final ShipStationRestClient CLIENT = new NoopShipStationRestClient();

        @Bean
        ShipStationRestClient shipStationRestClient() {
            return CLIENT;
        }
    }

    @Configuration(proxyBeanMethods = false)
    static class CustomInfrastructureConfiguration {
        static final ObjectMapper MAPPER = new ObjectMapper();
        static final RestClient REST_CLIENT = RestClient.builder().baseUrl("https://example.com").build();

        @Bean("shipStationRestObjectMapper")
        ObjectMapper shipStationRestObjectMapper() {
            return MAPPER;
        }

        @Bean("shipStationRestClientDelegate")
        RestClient shipStationRestClientDelegate() {
            return REST_CLIENT;
        }
    }

    static class NoopShipStationRestClient implements ShipStationRestClient {
        @Override
        public com.shipstation.api.rest.model.OrdersList getOrders() {
            return null;
        }

        @Override
        public com.shipstation.api.rest.model.OrdersList getOrders(java.util.Map<String, Object> params) {
            return null;
        }

        @Override
        public com.shipstation.api.rest.model.ShipStationOrder getOrder(Long orderId) {
            return null;
        }

        @Override
        public com.shipstation.api.rest.model.ShipStationOrder createOrUpdateOrder(com.shipstation.api.rest.model.ShipStationOrder order) {
            return null;
        }

        @Override
        public com.shipstation.api.rest.model.ShipmentsList getShipments() {
            return null;
        }

        @Override
        public com.shipstation.api.rest.model.ShipmentsList getShipments(java.util.Map<String, Object> params) {
            return null;
        }
    }
}