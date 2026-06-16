package com.shipstation.api.rest.support;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class ShipStationObjectMapperFactory {
    private ShipStationObjectMapperFactory() {
    }

    public static ObjectMapper create() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(shipStationJavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        return mapper;
    }

    private static JavaTimeModule shipStationJavaTimeModule() {
        JavaTimeModule module = new JavaTimeModule();
        module.addDeserializer(OffsetDateTime.class, new ShipStationOffsetDateTimeDeserializer());
        return module;
    }

    private static final class ShipStationOffsetDateTimeDeserializer extends JsonDeserializer<OffsetDateTime> {
        private static final Pattern OFFSET_WITHOUT_COLON = Pattern.compile("(.+[T ].*)([+-]\\d{2})(\\d{2})$");
        private static final Pattern MICROSOFT_JSON_DATE = Pattern.compile("/Date\\((-?\\d+)([+-]\\d{4})?\\)/");

        @Override
        public OffsetDateTime deserialize(JsonParser parser, DeserializationContext context) throws IOException {
            String value = parser.getValueAsString();
            if (value == null || value.isBlank()) {
                return null;
            }

            value = value.trim();
            if ("null".equalsIgnoreCase(value)) {
                return null;
            }

            OffsetDateTime microsoftDate = parseMicrosoftJsonDate(value);
            if (microsoftDate != null) {
                return microsoftDate;
            }

            String normalizedValue = normalizeOffset(value);
            try {
                return OffsetDateTime.parse(normalizedValue, DateTimeFormatter.ISO_OFFSET_DATE_TIME);
            } catch (DateTimeParseException ignored) {
                try {
                    return LocalDateTime.parse(value, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                            .atOffset(ZoneOffset.UTC);
                } catch (DateTimeParseException e) {
                    try {
                        return LocalDate.parse(value, DateTimeFormatter.ISO_LOCAL_DATE)
                                .atStartOfDay()
                                .atOffset(ZoneOffset.UTC);
                    } catch (DateTimeParseException dateOnlyException) {
                        throw InvalidFormatException.from(
                                parser,
                                "Expected an ISO offset date-time, a ShipStation local date-time, a ShipStation local date, or a Microsoft JSON date: " + value,
                                value,
                                OffsetDateTime.class
                        );
                    }
                }
            }
        }

        private String normalizeOffset(String value) {
            Matcher matcher = OFFSET_WITHOUT_COLON.matcher(value);
            if (!matcher.matches()) {
                return value;
            }
            return matcher.group(1) + matcher.group(2) + ":" + matcher.group(3);
        }

        private OffsetDateTime parseMicrosoftJsonDate(String value) {
            Matcher matcher = MICROSOFT_JSON_DATE.matcher(value);
            if (!matcher.matches()) {
                return null;
            }
            long epochMillis = Long.parseLong(matcher.group(1));
            return Instant.ofEpochMilli(epochMillis).atOffset(ZoneOffset.UTC);
        }
    }
}
