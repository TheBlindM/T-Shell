package com.tshell.module.dto.globalVariable;

import com.tshell.module.entity.GlobalVariable;

import javax.validation.constraints.NotBlank;

/**
 * @author TheBlind
 * @version 1.0
 */
public record AddGlobalVar(
        @NotBlank(message = "变量名称不能为空")
        String varName,
        @NotBlank(message = "值不能为空")
        String value) {
    public GlobalVariable convert() {
        return GlobalVariable.builder().value(this.value.strip()).varName(this.varName.strip()).build();
    }
}
