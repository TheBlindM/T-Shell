package com.tshell.core.tty;

import cn.hutool.core.lang.Assert;
import com.tshell.core.Parameter;
import com.jcraft.jsch.JSchException;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.util.HashMap;
import java.util.Objects;
import java.util.Optional;


@Singleton
public class TtyConnectorPool {

    @Inject
    TtyConnectorFactory ttyConnectorFactory;
    private final HashMap<String, TtyConnector> CHANNEL_POOL = HashMap.newHashMap(1);


    public TtyConnector getConnector(Parameter parameter) {
        String channelId = parameter.getChannelId();
        TtyConnector ttyConnector = CHANNEL_POOL.get(channelId);
        if (ttyConnector == null || !ttyConnector.isConnected()) {
            try {
                ttyConnector = ttyConnectorFactory.getTtyConnector(parameter);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            CHANNEL_POOL.put(channelId, ttyConnector);
            return ttyConnector;
        }
        return ttyConnector;
    }

    public int getSessionCount(String sessionId) {
        return (int) CHANNEL_POOL.values().stream().filter(tyConnector -> Objects.equals(sessionId, tyConnector.getSessionId())).count();
    }


    public TtyConnector getConnector(String channelId) {
        TtyConnector ttyConnector = CHANNEL_POOL.get(channelId);
        Assert.notNull(ttyConnector, "channelId  不存在");
        return ttyConnector;
    }

    public void close(String sessionId) {
        Optional.ofNullable(CHANNEL_POOL.remove(sessionId)).orElseThrow().close();
    }


}