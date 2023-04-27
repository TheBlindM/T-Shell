package com.tshell.module.dto.globalVariable;

import com.tshell.module.entity.GlobalVariable;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * @author TheBlind
 * @version 1.0
 */
public record UpdGlobalVar(
        @NotNull(message = "id不能为空")
        Integer id,
        @NotBlank(message = "变量名称不能为空")
        String varName,
        @NotBlank(message = "值不能为空")
        String value) {
    public void copyProperty(GlobalVariable source) {
        source.setVarName(this.varName);
        source.setValue(this.value);
    }
}
