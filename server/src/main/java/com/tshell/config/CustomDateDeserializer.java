package com.tshell.config;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import jakarta.enterprise.context.ApplicationScoped;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * @author TheBlind
 */
@ApplicationScoped

public class CustomDateDeserializer extends JsonDeserializer<LocalDate> {

    @Override
    public LocalDate deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {

        return  LocalDateTime.ofInstant(Instant.ofEpochMilli(Long.parseLong(jsonParser.getText())), ZoneId.systemDefault()) .toLocalDate();
    }
}