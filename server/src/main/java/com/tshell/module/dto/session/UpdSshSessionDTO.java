package com.tshell.module.dto.session;

import com.tshell.module.entity.Session;
import com.tshell.module.entity.SshSession;
import com.tshell.module.enums.AuthType;
import com.tshell.module.enums.ProxyType;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

public record UpdSshSessionDTO(
        @NotBlank(message = "id不能为null")
        String sessionId,
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
        @NotNull(message = "终端类型不能为空")
        Integer ttyTypeId,
        @NotNull(message = "组不能为空")
        Integer sessionGroupId,
        String proxyHost,
        Integer proxyPort,
        AuthType authType,
        ProxyType proxyType,
        String privateKeyFile,
        String passphrase
) {
    public void copyProperty(SshSession source) {
        source.setIp(this.ip);
        source.setPort(this.port);
        source.setUsername(this.username);
        source.setPwd(this.pwd);
        source.setAuthType(this.authType);
        source.setProxyHost(this.proxyHost);
        source.setProxyPort(this.proxyPort);
        source.setProxyType(this.proxyType);
        source.setPrivateKeyFile(this.privateKeyFile);
        source.setPassphrase(this.passphrase);
    }

    public void copyProperty(Session source) {
        source.setSessionName(this.sessionName);
        source.setTtyTypeId(this.ttyTypeId);
        source.setSessionGroupId(this.sessionGroupId);
    }

}
