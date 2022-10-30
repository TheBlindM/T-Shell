package com.tshell.module.vo;

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
        int ttyTypeId
) {
}
