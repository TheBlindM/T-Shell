package com.tshell.core;

import lombok.Data;

/**
 * @author TheBlind
 * @date 2022/7/21
 */

@Data
public class Parameter {

    protected String sessionId;
    protected String channelId;
    protected TtySize ttySize;
    protected Integer ttyTypeId;

    @Data
    public static class SshParameter extends Parameter {
        protected String ip;
        protected int port;
        protected String username;
        protected String pwd;

    }
}



