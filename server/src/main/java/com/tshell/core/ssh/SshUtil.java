package com.tshell.core.ssh;


import lombok.extern.slf4j.Slf4j;
import org.apache.sshd.client.SshClient;
import org.apache.sshd.client.channel.ChannelShell;
import org.apache.sshd.client.future.ConnectFuture;
import org.apache.sshd.client.session.ClientSession;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * 多个channel 不能同时发送命令
 *
 * @author TheBlind
 * @date 2022/4/2
 */


@Slf4j
public class SshUtil {

   static  SshClient client;

    public static ClientSession openSession(String host, int port, String user, String pwd, int timeout) throws IOException {
        client = SshClient.setUpDefaultClient();
        client.start();
        ConnectFuture connectFuture = client.connect(user, host, port).verify(10 * 1000);

        ClientSession session = connectFuture.getSession();
        session.addPasswordIdentity(pwd);
        session.auth().verify(TimeUnit.SECONDS.toMillis(timeout));
        Set<ClientSession.ClientSessionEvent> event = session.waitFor(
                Set.of(ClientSession.ClientSessionEvent.WAIT_AUTH,
                        ClientSession.ClientSessionEvent.CLOSED,
                        ClientSession.ClientSessionEvent.AUTHED), 0);

        if (!event.contains(ClientSession.ClientSessionEvent.AUTHED)) {
            log.debug("Session closed {} {}", event, session.isClosed());
            throw new RuntimeException("无法验证与设备的会话 " +
                    host + "check the user/pwd or key");
        }
        return session;
    }

    public static ChannelShell createShellChannel(ClientSession session) throws IOException {

      return session.createShellChannel();
    }

/*    public static ChannelShell createExecChannel(ClientSession session) throws IOException {
        return session.createExecChannel();
    }*/



}
