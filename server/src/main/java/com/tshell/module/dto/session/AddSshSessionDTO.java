package com.tshell.module.dto.session;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.tshell.module.entity.Session;
import com.tshell.module.entity.SshSession;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

public record AddSshSessionDTO(
        @NotBlank(message = "主机名称不能为空")
        String hostName,
        @NotBlank(message = "ip不能为空")
        String ip,
        @NotNull(message = "端口不能为空")
        @Min(value = 0, message = "端口不允许为负数")
        @Max(value = 65535, message = "端口不能大于65535")
        Integer port,
        @NotBlank(message = "主机名称不能为空")
        String username,
        @NotBlank(message = "密码不能为空")
        String pwd,
        @NotNull(message = "终端类型不能为空")
        Integer ttyTypeId,
        @NotNull(message = "组不能为空")
        Integer sessionGroupId
) {
    public Session convertSession() {
        return Session.builder().sessionName(hostName).ttyTypeId(ttyTypeId).sessionGroupId(sessionGroupId).build();
    }

    public SshSession convertSshSession() {
        return SshSession.builder().ip(ip).port(port).username(username).pwd(pwd).build();
    }
}
