package com.tshell.core;

import com.tshell.core.client.TtyType;
import com.tshell.core.tty.TtyConnectorPool;
import com.tshell.core.tty.TtyConnector;
import com.tshell.module.dto.terminal.LocalInitConnectDTO;
import com.tshell.module.dto.terminal.SshInitConnectDTO;
import com.tshell.module.entity.Session;
import com.tshell.module.entity.SshSession;
import com.tshell.service.ConnectionLogService;
import com.tshell.socket.WebSocket;
import com.jcraft.jsch.JSchException;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.WebApplicationException;
import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

/**
 * @author TheBlind
 */
@Singleton
@Slf4j
public class TerminalService {

    private final int DEFAULT_BUFFER_SIZE = 60;

    final ExecutorService executorService = Executors.newVirtualThreadPerTaskExecutor();

    @Inject
    TtyConnectorPool ttyConnectorPool;

    @Inject
    FileManagerService fileManagerService;


    private Parameter.SshParameter convertToSshParameter(SshInitConnectDTO sshInitConnectDTO) {
        String sessionId = sshInitConnectDTO.sessionId();
        Session session = Session.<Session>findById(sessionId);
        SshSession sshSession = SshSession.<SshSession>find("sessionId = ?1", sessionId).singleResultOptional().orElseThrow(() -> new WebApplicationException("host with sessionId of not exist", 404));


        Parameter.SshParameter sshParameter = new Parameter.SshParameter();
        sshParameter.setSessionId(sessionId);
        sshParameter.setIp(sshSession.getIp());
        sshParameter.setPort(sshSession.getPort());
        sshParameter.setUsername(sshSession.getUsername());
        sshParameter.setPwd(sshSession.getPwd());
        sshParameter.setChannelId(sshInitConnectDTO.channelId());
        sshParameter.setTtySize(sshInitConnectDTO.ttySize());
        sshParameter.setTtyTypeId(session.getTtyTypeId());
        return sshParameter;
    }

    private Parameter convertToLocalParameter(LocalInitConnectDTO localInitConnectDTO) {
        Parameter parameter = new Parameter();
        parameter.setSessionId(localInitConnectDTO.sessionId());
        parameter.setChannelId(localInitConnectDTO.channelId());
        parameter.setTtySize(localInitConnectDTO.ttySize());
        return parameter;
    }

    /**
     * 初始化 ssh 连接
     *
     * @throws IOException
     * @throws JSchException
     */
    public void initSshConnection(SshInitConnectDTO sshInitConnectDTO) {
        Parameter.SshParameter sshParameter = this.convertToSshParameter(sshInitConnectDTO);
        initConnection(sshParameter);
    }


    /**
     * 初始化 连接
     *
     * @param localInitConnectDTO 主机
     */
    public void initLocalConnection(LocalInitConnectDTO localInitConnectDTO) {
        Parameter parameter = this.convertToLocalParameter(localInitConnectDTO);
        initConnection(parameter);

    }

    @Inject
    ConnectionLogService connectionLogService;

    private void initConnection(Parameter parameter) {
        TtyConnector ttyConnector = ttyConnectorPool.getConnector(parameter);
        connectionLogService.start(parameter.sessionId);
        String channelId = parameter.getChannelId();
        receiveMsg((result) -> {
            WebSocket.sendMsg(channelId, WebSocket.MsgType.CMD, result);
        }, ttyConnector);
    }


    private void receiveMsg(Consumer<String> msgHandle, TtyConnector ttyConnector) {
        executorService.execute(() -> {
            char[] buffer = new char[DEFAULT_BUFFER_SIZE];
            int i = 0;
            while (true) {
                try {
                    if ((i = ttyConnector.read(buffer)) == -1) {
                        msgHandle.accept("当前通道已关闭");
                        break;
                    }
                } catch (IOException e) {
                    msgHandle.accept("通道异常关闭:" + e.getMessage());
                    throw new RuntimeException(e);
                }
                msgHandle.accept(new String(buffer, 0, i));
            }

        });
    }

    public void executeCmd(String key, String cmd) throws Exception {
        ttyConnectorPool.getConnector(key).write(cmd);
    }


    public void changeWindow(String key, TtySize ttySize) throws IOException {
        getTyConnector(key).resize(ttySize);
    }


    public void close(String channelId) {
        TtyConnector connector = ttyConnectorPool.getConnector(channelId);
        int sessionCount = ttyConnectorPool.getSessionCount(connector.getSessionId());
        if (sessionCount == 1) {
            fileManagerService.pauseTransfer(channelId);
        }
        connector.close();
    }

    public boolean isInCommandInput(String channelId) {
        return getTyConnector(channelId).isInCommandInput();
    }

    public TtyType getTtyOsType(String channelId) {
        return getTyConnector(channelId).getTtyOsType();
    }


    private TtyConnector getTyConnector(String channelId) {
        return Optional.ofNullable(ttyConnectorPool.getConnector(channelId)).orElseThrow();
    }

    public int getSessionCount(String channelId) {
        String sessionId = getTyConnector(channelId).getSessionId();
        return ttyConnectorPool.getSessionCount(sessionId);
    }


}
