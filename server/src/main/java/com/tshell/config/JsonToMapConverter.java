package com.tshell.config;

import cn.hutool.json.JSONUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tshell.module.entity.Config;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.io.IOException;

/**
 * @author TheBlind
 */
@Converter
public class JsonToMapConverter
        implements AttributeConverter<Config.Terminal, String> {

    @Override
    public String convertToDatabaseColumn(Config.Terminal terminal) {
        return JSONUtil.toJsonStr(terminal);
    }

    @Override
    public Config.Terminal convertToEntityAttribute(String dbData) {

        if (dbData == null) {
            return new Config.Terminal();
        }
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.readValue(dbData, Config.Terminal.class);
        } catch (IOException e) {
            new RuntimeException(e);
        }
        return new Config.Terminal();
    }
}
