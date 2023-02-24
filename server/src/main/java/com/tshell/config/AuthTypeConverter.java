package com.tshell.config;

import com.tshell.module.enums.AuthType;
import com.tshell.utils.enums.EnumUtil;

import javax.persistence.AttributeConverter;

/**
 * @author TheBlind
 * @version 1.0
 */
public class AuthTypeConverter implements AttributeConverter<AuthType, Integer> {
    @Override
    public Integer convertToDatabaseColumn(AuthType authType) {
        return authType.getValue();
    }

    @Override
    public AuthType convertToEntityAttribute(Integer value) {
        //默认为 密码模式
        if (value == null) {
            return AuthType.PWD;
        }
        return EnumUtil.getEnumByValue(AuthType.values(), value);
    }
}
