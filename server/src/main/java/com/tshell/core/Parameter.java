package com.tshell.core;

import com.tshell.module.enums.AuthType;
import lombok.Data;

/**
 * https://www.lmlphp.com/user/59001/article/item/600745/
 *
 * https://www.cnblogs.com/kxqblog/p/16143152.html  mobaxterm  怎么设置私钥登录
 *
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
        protected String privateKeyFile;
        protected String passphrase;
        protected AuthType authType;
    }


}



