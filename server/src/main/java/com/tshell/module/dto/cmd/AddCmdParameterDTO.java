package com.tshell.module.dto.cmd;

import com.tshell.module.entity.Parameter;

/**
 * @author TheBlind
 * @date 2022/7/12
 */
public record AddCmdParameterDTO(
        /**
         * 参数下标
         */
        int index,
        /**
         * 描述
         */
        String description) {

    public Parameter convert(){
        return   Parameter.builder().index(this.index).description(this.description).build();
    }

}
