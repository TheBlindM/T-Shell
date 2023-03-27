package com.tshell.module.enums;

import com.tshell.utils.enums.ValueEnum;

/**
 * @author TheBlind
 * @version 1.0
 */
public enum ProxyType implements ValueEnum<Integer> {
    /**
     * 直连
     */
    DIRECT(0),

    /**
     * http
     */
    HTTP(1),
    /**
     * socket
     */
    SOCKET(2);

    ProxyType(int value) {
        this.value = value;
    }

    private int value;

    @Override
    public Integer getValue() {
        return this.value;
    }
}

