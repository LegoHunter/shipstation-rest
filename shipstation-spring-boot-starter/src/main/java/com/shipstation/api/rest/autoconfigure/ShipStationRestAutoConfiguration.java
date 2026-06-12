package com.shipstation.api.rest.autoconfigure;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shipstation.api.rest.client.DefaultShipStationRestClient;
import com.shipstation.api.rest.client.ShipStationHttpClient;
import com.shipstation.api.rest.client.ShipStationRestClient;
import com.shipstation.api.rest.exception.ShipStationClientException;
import com.shipstation.api.rest.exception.ShipStationServerException;
import com.shipstation.api.rest.model.ShipStationError;
import com.shipstation.api.rest.support.ShipStationBasicAuthInterceptor;
import com.shipstation.api.rest.support.ShipStationHttpLoggingInterceptor;
import com.shipstation.api.rest.support.ShipStationObjectMapperFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@AutoConfiguration
@EnableConfigurationProperties(ShipStationRestProperties.class)
public class ShipStationRestAutoConfiguration {
    @Bean
    @ConditionalOnMissingBean(name = "shipStationRestObjectMapper")
    public ObjectMapper shipStationRestObjectMapper() {
        return ShipStationObjectMapperFactory.create();
    }

    @Bean
    @ConditionalOnMissingBean(name = "shipStationBasicAuthInterceptor")
    public ClientHttpRequestInterceptor shipStationBasicAuthInterceptor(ShipStationRestProperties properties) {
        return new ShipStationBasicAuthInterceptor(properties.getApiKey(), properties.getApiSecret());
    }

    @Bean
    @ConditionalOnMissingBean(name = "shipStationRestClientDelegate")
    public RestClient shipStationRestClientDelegate(
            ShipStationRestProperties properties,
            @Qualifier("shipStationRestObjectMapper") ObjectMapper objectMapper,
            @Qualifier("shipStationBasicAuthInterceptor") ClientHttpRequestInterceptor authInterceptor
    ) {
        RestClient.Builder builder = RestClient.builder()
                .baseUrl(properties.getUri().toString())
                .messageConverters(converters -> {
                    converters.removeIf(MappingJackson2HttpMessageConverter.class::isInstance);
                    converters.add(new MappingJackson2HttpMessageConverter(objectMapper));
                })
                .requestInterceptor(authInterceptor)
                .defaultStatusHandler(statusCode -> statusCode.is3xxRedirection() || statusCode.isError(), (request, response) -> {
                    int statusCode = response.getStatusCode().value();
                    ShipStationError error = error(response, objectMapper);
                    if (statusCode >= 300 && statusCode <= 499) {
                        String requestDescription = request.getMethod() + " " + request.getURI();
                        if (statusCode >= 300 && statusCode <= 399) {
                            error.setMessage("Unexpected redirect to [%s]".formatted(response.getHeaders().getFirst(HttpHeaders.LOCATION)));
                        }
                        throw new ShipStationClientException(statusCode, requestDescription, error);
                    }
                    throw new ShipStationServerException(statusCode, error);
                });

        if (properties.getHttpLogging().isEnabled()) {
            builder.requestFactory(new BufferingClientHttpRequestFactory(new SimpleClientHttpRequestFactory()));
            builder.requestInterceptor(new ShipStationHttpLoggingInterceptor(properties.getHttpLogging()));
        }

        return builder.build();
    }

    @Bean
    @ConditionalOnMissingBean
    public ShipStationHttpClient shipStationHttpClient(
            @Qualifier("shipStationRestClientDelegate") RestClient shipStationRestClientDelegate
    ) {
        RestClientAdapter adapter = RestClientAdapter.create(shipStationRestClientDelegate);
        return HttpServiceProxyFactory.builderFor(adapter)
                .build()
                .createClient(ShipStationHttpClient.class);
    }

    @Bean
    @ConditionalOnMissingBean
    public ShipStationRestClient shipStationRestClient(ShipStationHttpClient shipStationHttpClient) {
        return new DefaultShipStationRestClient(shipStationHttpClient);
    }

    private static ShipStationError error(ClientHttpResponse response, ObjectMapper objectMapper) throws IOException {
        byte[] bytes = response.getBody().readAllBytes();
        if (bytes.length == 0) {
            return ShipStationError.builder().message(response.getStatusText()).build();
        }
        try {
            return objectMapper.readValue(bytes, ShipStationError.class);
        } catch (IOException e) {
            return ShipStationError.builder()
                    .message(new String(bytes, StandardCharsets.UTF_8))
                    .deserializationErrorMessage(e.getMessage())
                    .build();
        }
    }
}
