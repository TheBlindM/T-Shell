package com.tshell.core.ssh.jsch;

import com.tshell.utils.enums.ValueEnum;

/**
 * @author TheBlind
 * @date 2022/7/29
 */
public enum ChannelType implements ValueEnum<String> {
    /**
     * 会话
     */
    SESSION("session"),
    /**
     * shell
     */
    SHELL("shell"),
    /**
     * 执行命令
     */
    EXEC("exec"),
    /**
     * 直连
     */
    X11("x11"),
    /**
     * 直连
     */
    AGENT_FORWARDING("auth-agent@openssh.com"),
    /**
     * 直连
     */
    DIRECT_TCPIP("direct-tcpip"),
    /**
     * 转发TCP ip
     */
    FORWARDED_TCPIP("forwarded-tcpip"),
    /**
     * sftp
     */
    SFTP("sftp"),
    /**
     * 子系统
     */
    SUBSYSTEM("subsystem");

    private final String value;

    ChannelType(String value) {
        this.value = value;
    }

    @Override
    public String getValue() {
        return this.value;
    }
}

