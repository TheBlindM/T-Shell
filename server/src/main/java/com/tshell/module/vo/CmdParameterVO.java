package com.tshell.module.vo;

/**
 * @author TheBlind
 * @date 2022/7/12
 */
public record CmdParameterVO(
        Integer id,
        /**
         * 参数下标
         */
        int index,
        /**
         * 描述
         */
        String description) {
}
