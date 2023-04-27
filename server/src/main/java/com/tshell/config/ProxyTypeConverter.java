package com.tshell.config;

import com.tshell.module.enums.ProxyType;
import com.tshell.utils.enums.EnumUtil;

import jakarta.persistence.AttributeConverter;

/**
 * @author TheBlind
 * @version 1.0
 */
public class ProxyTypeConverter implements AttributeConverter<ProxyType, Integer> {
    @Override
    public Integer convertToDatabaseColumn(ProxyType authType) {
        return authType.getValue();
    }

    @Override
    public ProxyType convertToEntityAttribute(Integer value) {
        //默认为 密码模式
        if (value == null) {
            return ProxyType.DIRECT;
        }
        return EnumUtil.getEnumByValue(ProxyType.values(), value);
    }
}
