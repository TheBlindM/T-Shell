package com.tshell.module.dto.cmd;

import com.tshell.module.entity.Parameter;

/**
 * @author TheBlind
 * @date 2022/7/12
 */
public record UpdCmdParameterDTO(
        Integer id,
        /**
         * 参数下标
         */
        int index,
        /**
         * 描述
         */
        String description) {

    public Parameter convert() {
        Parameter parameter = Parameter.builder().index(this.index).description(this.description).build();
        parameter.id = id;
        return parameter;
    }

}
