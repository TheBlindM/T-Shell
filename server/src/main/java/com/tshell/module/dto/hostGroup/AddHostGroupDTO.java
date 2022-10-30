package com.tshell.module.dto.hostGroup;

import com.tshell.module.entity.SessionGroup;

import javax.validation.constraints.NotBlank;

public record AddHostGroupDTO(
        @NotBlank(message = "组名称不能为空")
        String groupName,
        int parentId
) {
        public SessionGroup convert() {
                return SessionGroup.builder().groupName(groupName).parentId(parentId).build();
        }
}
