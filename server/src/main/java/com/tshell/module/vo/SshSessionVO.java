package com.tshell.module.vo;

import com.tshell.module.enums.AuthType;
import com.tshell.module.enums.ProxyType;
import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public record SshSessionVO(
        String sessionId,
        String ip,
        int port,
        String username,
        String pwd,
        int sessionGroupId,
        String sessionName,
        int ttyTypeId,
        AuthType authType,
        String privateKeyFile,
        String passphrase,
        String proxyHost,
        Integer proxyPort,
        ProxyType proxyType
) {
}
