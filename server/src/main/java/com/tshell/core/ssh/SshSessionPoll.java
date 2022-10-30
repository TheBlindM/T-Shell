package com.tshell.core.ssh;

import cn.hutool.core.lang.Assert;
import org.apache.sshd.client.session.ClientSession;


import java.io.IOException;
import java.util.HashMap;

/**
 * @author TheBlind
 */
public enum SshSessionPoll {
    INSTANCE;

    private final HashMap<String, ClientSession> cache = new HashMap<>();


    public ClientSession getSession(String sessionId, String host, int port, String user, String pwd, int timeout) throws IOException {
        ClientSession session = cache.get(sessionId);
        if (session == null ||session.isClosed()) {
            session = SshUtil.openSession(host, port, user, pwd, timeout);
            cache.put(sessionId, session);
            return session;
        }
        return session;
    }

    public ClientSession getSession(String sessionId) {
        ClientSession session = cache.get(sessionId);
        Assert.notNull(session, "sessionId  不存在");
        return session;
    }

    public void close(String sessionId) {
        ClientSession session = cache.get(sessionId);
        if (session != null && !session.isClosed()) {
            session.close(false);
        }
    }


}
