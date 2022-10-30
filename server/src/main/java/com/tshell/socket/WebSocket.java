package com.tshell.socket;

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

    final ExecutorService executorService = Executors.newVirtualThreadPerTaskExecutor();

    @RegisterForReflection
    public enum MsgType {
        PROGRESS, CMD, RETRIEVE_CMD
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


    private static Session session;
    @Inject
    ShellSocketHandler shellSocketHandler;


    public static void sendMsg(Msg msg) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            WebSocket.session.getAsyncRemote().sendText(objectMapper.writeValueAsString(msg));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public static void sendMsg(String channelId, MsgType msgType, String message) {
        sendMsg(new Msg(channelId, msgType, message));
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
