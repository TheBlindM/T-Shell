package com.tshell.core;

import com.tshell.core.client.TtyType;
import com.tshell.core.tty.TtyConnectorManager;
import com.tshell.core.tty.TtyConnector;
import com.tshell.module.dto.terminal.LocalInitConnectDTO;
import com.tshell.module.dto.terminal.SshInitConnectDTO;
import com.tshell.module.entity.Session;
import com.tshell.module.entity.SshSession;
import com.tshell.service.SyncChannelService;
import com.tshell.service.ConnectionLogService;
import com.tshell.socket.WebSocket;
import lombok.extern.slf4j.Slf4j;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.ws.rs.WebApplicationException;
import java.io.IOException;
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
    TtyConnectorManager ttyConnectorManager;

    @Inject
    FileManagerService fileManagerService;

    @Inject
    ConnectionLogService connectionLogService;

    @Inject
    SyncChannelService syncChannelService;


    private Parameter.SshParameter convertToSshParameter(SshInitConnectDTO sshInitConnectDTO) {
        String sessionId = sshInitConnectDTO.sessionId();
        Session session = Session.<Session>findById(sessionId);
        SshSession sshSession = SshSession.<SshSession>find("sessionId = ?1", sessionId).singleResultOptional().orElseThrow(() -> new WebApplicationException("host with sessionId of not exist", 404));


        Parameter.SshParameter sshParameter = new Parameter.SshParameter();
        sshParameter.setSessionId(sessionId);
        sshParameter.setIp(sshSession.getIp());
        sshParameter.setPort(sshSession.getPort());
        // 验证方式
        switch (sshSession.getAuthType()) {
            case PUBLIC_KEY -> {
                sshParameter.setUsername(sshSession.getUsername());
                sshParameter.setPrivateKeyFile(sshSession.getPrivateKeyFile());
                sshParameter.setPassphrase(sshSession.getPassphrase());
            }
            case KEYBOARD_INTERACTIVE -> {
                sshParameter.setUsername(sshInitConnectDTO.username());
                sshParameter.setPwd(sshInitConnectDTO.pwd());
            }
            case PWD -> {
                sshParameter.setUsername(sshSession.getUsername());
                sshParameter.setPwd(sshSession.getPwd());
            }
        }
        switch (sshSession.getProxyType()) {
            case HTTP, SOCKET -> {
                sshParameter.setProxyHost(sshSession.getProxyHost());
                sshParameter.setProxyPort(sshSession.getProxyPort());
            }
        }

        sshParameter.setAuthType(sshSession.getAuthType());
        sshParameter.setProxyType(sshSession.getProxyType());
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


    private void initConnection(Parameter parameter) {
        TtyConnector ttyConnector = ttyConnectorManager.getConnector(parameter.getChannelId()).orElse(ttyConnectorManager.create(parameter));
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



    public void executeCmd(String key, String cmd) {
        syncChannelService.getSshChannelIds(key).ifPresentOrElse((sshChannelIds) -> {
            sshChannelIds.forEach((sshChannelId) -> {
                ttyConnectorManager.getConnector(sshChannelId).ifPresent((ttyConnector -> {
                    try {
                        ttyConnector.write(cmd);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }));
            });

        }, () -> {
            ttyConnectorManager.getConnector(key).ifPresent((ttyConnector -> {
                try {
                    ttyConnector.write(cmd);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }));
        });

    }


    public void changeWindow(String key, TtySize ttySize) throws IOException {
        getTyConnector(key).resize(ttySize);
    }


    public void close(String channelId) {
        ttyConnectorManager.getConnector(channelId).ifPresent(ttyConnector -> {
            syncChannelService.remove(channelId);
            int sessionCount = ttyConnectorManager.getSessionCount(ttyConnector.getSessionId());
            if (sessionCount == 1) {
                fileManagerService.pauseTransfer(channelId);
            }
            ttyConnectorManager.close(channelId);
        });
    }

    public boolean isInCommandInput(String channelId) {
        return getTyConnector(channelId).isInCommandInput();
    }

    public TtyType getTtyOsType(String channelId) {
        return getTyConnector(channelId).getTtyOsType();
    }


    private TtyConnector getTyConnector(String channelId) {
        return ttyConnectorManager.getConnector(channelId).orElseThrow();
    }

    public int getSessionCount(String channelId) {
        String sessionId = getTyConnector(channelId).getSessionId();
        return ttyConnectorManager.getSessionCount(sessionId);
    }

}
