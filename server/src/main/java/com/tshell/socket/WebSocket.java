package com.tshell.socket;

import cn.hutool.cache.CacheUtil;
import cn.hutool.cache.impl.TimedCache;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.extern.slf4j.Slf4j;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


/**
 * @author TheBlind
 * @date 2022/4/1
 */

@ServerEndpoint("/msg")
@ApplicationScoped
@Slf4j
public class WebSocket {

    static final TimedCache<String, Object> timedCache = CacheUtil.newTimedCache(1000);
    private static Session session;
    final ExecutorService executorService = Executors.newVirtualThreadPerTaskExecutor();
    @Inject
    ShellSocketHandler shellSocketHandler;

    @RegisterForReflection
    public enum MsgType {
        PROGRESS, CMD, RETRIEVE_CMD,OPEN_FILE,OPEN_FILE_PROGRESS
    }

    @RegisterForReflection
    public record Msg(
            @JsonProperty("channelId")
            String channelId,
            @JsonProperty("msgType")
            MsgType msgType,
            @JsonProperty("message")
            String message
    ) {
    }


    /**
     * @param msg 消息
     */
    public static void sendMsg(Msg msg) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            WebSocket.session.getAsyncRemote().sendText(objectMapper.writeValueAsString(msg));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 发送消息
     * @param channelId 通道id
     * @param msgType 消息类型
     * @param message 消息
     */
    public static void sendMsg(String channelId, MsgType msgType, String message) {
        sendMsg(new Msg(channelId, msgType, message));
    }


    /**
     * 发送间隔消息,避免消息发送太过频繁,默认为1000
     * @param channelId 通道id
     * @param msgType 消息类型
     * @param message 消息
     * @param key 缓存key
     */
    public static void sendIntervalMsg(String channelId, MsgType msgType, String message, String key) {
        sendIntervalMsg(channelId, msgType, message, key,1000);
    }

    /**
     * 发送间隔消息,避免消息发送太过频繁
     * @param channelId 通道id
     * @param msgType 消息类型
     * @param message 消息
     * @param key 缓存key
     * @param timeout 超时时间
     */
    public static void sendIntervalMsg(String channelId, MsgType msgType, String message, String key, long timeout) {
        if (!timedCache.containsKey(key)) {
            timedCache.put(key, null, timeout);
            sendMsg(new Msg(channelId, msgType, message));
        }
    }

    @OnOpen
    public void onOpen(Session session) {
        log.info("onOpen");
        WebSocket.session = session;
    }

    @OnClose
    public void onClose(Session session) {
        WebSocket.session = null;
    }

    @OnError
    public void onError(Session session, Throwable throwable) {
        WebSocket.session = null;
    }


    @OnMessage
    public void onMessage(String message) {
        executorService.execute(() -> {

            try {
                shellSocketHandler.messageHandle(message);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });


    }


}
