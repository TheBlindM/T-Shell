package com.tshell.socket;

import cn.hutool.core.date.TimeInterval;
import com.tshell.core.TerminalService;
import com.tshell.service.RetrieveService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;

/**
 * @author TheBlind
 * @date 2022/6/21
 */

@Singleton
@Slf4j
public class ShellSocketHandler {


    @Inject
    TerminalService terminalService;

    @Inject
    RetrieveService retrieveService;


    @Inject
    ObjectMapper objectMapper;

    /**
     * 消息处理
     *
     * @param message
     * @throws Exception
     */
    public void messageHandle(String message) throws Exception {
        TimeInterval timer = cn.hutool.core.date.DateUtil.timer();
        log.debug("message" + message);
        WebSocket.Msg shellMessage = objectMapper.readValue(message, WebSocket.Msg.class);
        log.debug("messageType" + shellMessage.msgType());
        switch (shellMessage.msgType()) {
            case CMD -> terminalService.executeCmd(shellMessage.channelId(), shellMessage.message());
            case RETRIEVE_CMD -> {
                log.debug("RETRIEVE_CMD enter");
                RetrieveCmdData retrieveCmdData = objectMapper.readValue(shellMessage.message(), RetrieveCmdData.class);
                try {
                    String jsonStr = objectMapper.writeValueAsString(retrieveService.retrieve(retrieveCmdData.term(), shellMessage.channelId(), retrieveCmdData.skipVerify()));
                    log.debug("RetrieveCmdData message" + jsonStr);
                    WebSocket.sendMsg(shellMessage.channelId(), WebSocket.MsgType.RETRIEVE_CMD, jsonStr);
                } catch (Exception e) {
                    log.debug(e.getMessage());
                    throw new RuntimeException(e);
                }

            }
        }

        long interval = timer.interval();
        log.debug("耗时" + interval);

        System.out.println("Integer" + interval);

    }


}
