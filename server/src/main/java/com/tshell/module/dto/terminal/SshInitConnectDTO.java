package com.tshell.module.dto.terminal;

import com.tshell.core.TtySize;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * @author TheBlind
 * @date 2022/8/16
 */
public record SshInitConnectDTO(
        @NotBlank(message = "channelId不能为空")
        String channelId,
        @NotNull(message = "ptySize")
        TtySize ttySize,
        @NotNull(message = "sessionId不能为空")
        String sessionId
) {
}
