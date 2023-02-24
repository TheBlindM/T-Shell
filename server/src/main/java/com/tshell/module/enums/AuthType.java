package com.tshell.module.enums;

import com.tshell.utils.enums.ValueEnum;

/**
 * @author TheBlind
 * @version 1.0
 */
public enum AuthType implements ValueEnum<Integer> {
    /**
     * 密码方式
     */
    PWD(0),
    /**
     * 公私钥
     */
    PUBLIC_KEY(1),
    /**
     * 键盘互动模式
     */
    KEYBOARD_INTERACTIVE(2);

    AuthType(int value) {
        this.value = value;
    }

    private int value;

    @Override
    public Integer getValue() {
        return this.value;
    }
}
