package com.tshell.core.ssh;

/**
 *
 * ssh配置
 * @author TheBlind
 * @version 1.0
 *
 */
public record SshConfig(String ip,
                        int port,
                        String username,
                        String pwd) {
}
