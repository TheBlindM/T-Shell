package com.tshell.utils.enums;

import com.tshell.utils.StrUtil;

/**
 * @author TheBlind
 * @date 2022/8/2
 */
public class EnumUtil {


    /**
     * 根据枚举Key获取对应的枚举Value
     *
     * @param enums 枚举列表
     * @param key   枚举key
     * @param <T>   值类型
     * @return 枚举key
     */
    public static <T> T getValueByKey(KeyValueEnum<T>[] enums, String key) {
        if (StrUtil.isEmpty(key)) {
            return null;
        }
        for (KeyValueEnum<T> keyValueEnum : enums) {
            if (key.equals(keyValueEnum.getKey())) {
                return keyValueEnum.getValue();
            }
        }
        return null;
    }


    /**
     * 根据枚举Value获取对应的枚举Key
     *
     * @param enums 枚举列表
     * @param value 枚举值
     * @param <T>   值类型
     * @return 枚举key
     */
    public static <T> String getKeyByValue(KeyValueEnum<T>[] enums, T value) {
        if (value == null) {
            return null;
        }
        for (KeyValueEnum<T> keyValueEnum : enums) {
            if (value.equals(keyValueEnum.getValue())) {
                return keyValueEnum.getKey();
            }
        }
        return null;
    }


    /**
     * 根据枚举值获取对应的枚举对象
     *
     * @param enums 枚举列表
     * @return 枚举对象
     */
    public static <E extends ValueEnum<V>, V> E getEnumByValue(E[] enums, V value) {
        for (E e : enums) {
            if ( e.getValue().equals(value)) {
                return e;
            }
        }
        return null;
    }




}
