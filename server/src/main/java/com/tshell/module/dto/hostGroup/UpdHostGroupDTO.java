package com.tshell.module.dto.hostGroup;

import com.tshell.module.entity.SessionGroup;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UpdHostGroupDTO(
        @NotNull(message = "id不能为null")
        Integer id,
        @NotBlank(message = "组名称不能为空")
        String groupName,
        int parentId
) {
    public void copyProperty(SessionGroup source) {
        source.setGroupName(this.groupName);
        source.setParentId(parentId);
    }
}
