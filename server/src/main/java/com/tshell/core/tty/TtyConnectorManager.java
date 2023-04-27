package com.tshell.core.tty;

import com.tshell.core.Parameter;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.io.IOException;
import java.util.HashMap;
import java.util.Objects;
import java.util.Optional;


@Singleton
public class TtyConnectorManager {

    @Inject
    TtyConnectorFactory ttyConnectorFactory;
    private final HashMap<String, TtyConnector> CHANNEL_POOL = HashMap.newHashMap(1);


    public TtyConnector create(Parameter parameter){
        String channelId = parameter.getChannelId();
        TtyConnector ttyConnector = null;
        try {
            ttyConnector = ttyConnectorFactory.getTtyConnector(parameter);
            CHANNEL_POOL.put(channelId, ttyConnector);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return ttyConnector;
    }

    public Optional<TtyConnector> getConnector(String channelId) {
        return Optional.ofNullable(CHANNEL_POOL.get(channelId));
    }

    public int getSessionCount(String sessionId) {
        return (int) CHANNEL_POOL.values().stream().filter(tyConnector -> Objects.equals(sessionId, tyConnector.getSessionId())).count();
    }


    public void close(String channelId) {
        Optional.ofNullable(CHANNEL_POOL.remove(channelId)).ifPresent(TtyConnector::close);
    }


}