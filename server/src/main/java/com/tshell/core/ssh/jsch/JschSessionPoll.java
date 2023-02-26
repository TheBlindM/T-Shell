package com.tshell.core.ssh.jsch;

import cn.hutool.core.lang.Assert;
import com.jcraft.jsch.Session;

import java.util.HashMap;

/**
 * @author TheBlind
 * @date 2022/4/12
 */
@Deprecated
public enum JschSessionPoll {
    INSTANCE;

    private final HashMap<String, Session> cache = new HashMap<>();


    public Session getSession(String sessionId, String host, int port, String user, String pwd, int timeout) {
        return getSession(sessionId, host, port, user, pwd, timeout,false);
    }

    public Session getSession(String sessionId, String host, int port, String user, String pwd, int timeout,boolean create) {
        Session session = cache.get(sessionId);
        if (session == null || !session.isConnected()||create) {
            session = JschUtil.openSession(host, port, user, pwd, timeout);
            cache.put(sessionId, session);
            return session;
        }
        return session;
    }

    public Session getSession(String sessionId, String host, int port, String user, String privateKeyPath, String passphrase, int timeout,boolean create) {
        Session session = cache.get(sessionId);
        if (session == null || !session.isConnected()||create) {
            session = JschUtil.openSession(host, port, user, privateKeyPath,passphrase, timeout);
            cache.put(sessionId, session);
            return session;
        }
        return session;
    }




    public Session getSession(String sessionId) {
        Session session = cache.get(sessionId);
        Assert.notNull(session, "sessionId  不存在");
        return session;
    }

    public void close(String sessionId) {
        Session session = cache.get(sessionId);
        if (session != null && session.isConnected()) {
            session.disconnect();
        }
    }


}
