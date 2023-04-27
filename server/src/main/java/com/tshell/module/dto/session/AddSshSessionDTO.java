package com.tshell.module.dto.session;


import com.tshell.module.entity.Session;
import com.tshell.module.entity.SshSession;
import com.tshell.module.enums.AuthType;
import com.tshell.module.enums.ProxyType;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AddSshSessionDTO(
        @NotBlank(message = "主机名称不能为空")
        String sessionName,
        @NotBlank(message = "ip不能为空")
        String ip,
        @NotNull(message = "端口不能为空")
        @Min(value = 0, message = "端口不允许为负数")
        @Max(value = 65535, message = "端口不能大于65535")
        Integer port,

        String username,
        String pwd,
        String privateKeyFile,
        String passphrase,
        String proxyHost,
        Integer proxyPort,
        AuthType authType,
        ProxyType proxyType,

        @NotNull(message = "终端类型不能为空")
        Integer ttyTypeId,
        @NotNull(message = "组不能为空")
        Integer sessionGroupId
) {
    public Session convertSession() {
        return Session.builder().sessionName(sessionName).ttyTypeId(ttyTypeId).sessionGroupId(sessionGroupId).build();
    }

    public SshSession convertSshSession() {
        return SshSession.builder().ip(ip).port(port).username(username).pwd(pwd).authType(authType).proxyType(proxyType).proxyHost(proxyHost).proxyPort(proxyPort).privateKeyFile(privateKeyFile).passphrase(passphrase).build();
    }
}
