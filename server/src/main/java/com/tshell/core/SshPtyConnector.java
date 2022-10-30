package com.tshell.core;

import cn.hutool.core.lang.Assert;
import com.tshell.core.client.TtyType;
import com.tshell.core.ssh.SshSessionPoll;
import com.tshell.core.ssh.SshUtil;
import com.tshell.core.tty.TtyConnector;
import com.tshell.module.entity.SshSession;
import lombok.extern.slf4j.Slf4j;
import org.apache.sshd.client.channel.ChannelShell;
import org.apache.sshd.client.future.OpenFuture;
import org.apache.sshd.client.session.ClientSession;

import java.io.*;
import java.net.ConnectException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

/**
 * @author TheBlind
 * @date 2022/7/21
 */
@Slf4j
public class SshPtyConnector implements TtyConnector {

    protected final InputStreamReader myReader;
    protected final OutputStreamWriter myWriter;
    private final ChannelShell shellChannel;
    private final InputStream inputStream;
    private final OutputStream outputStream;

    public SshPtyConnector(Parameter.SshParameter parameter) throws IOException {
        SshSession sshSession = SshSession.<SshSession>findById(parameter.getSessionId());


        ClientSession session = SshSessionPoll.INSTANCE.getSession(parameter.getSessionId(), sshSession.getIp(), sshSession.getPort(), sshSession.getUsername(), sshSession.getPwd(), 3000);

        ChannelShell shellChannel = SshUtil.createShellChannel(session);

        TtySize ttySize = parameter.getTtySize();
        shellChannel.setPtyType("xterm-256color");
        shellChannel.setPtyColumns(ttySize.columns());
        shellChannel.setPtyLines(ttySize.lines());


        OpenFuture open = shellChannel.open();


        /*xterm-256color、xterm-color、xterm、linux、vt220、vt100*/
        if (open.await(1000, TimeUnit.SECONDS)) {
            shellChannel.getPtyColumns();
            shellChannel.getPtyLines();
            shellChannel.getPtyWidth();
            shellChannel.getPtyHeight();
            if (open.isOpened()) {
                this.shellChannel = shellChannel;
                this.inputStream = shellChannel.getInvertedOut();
                this.outputStream = shellChannel.getInvertedIn();

                this.myReader = new InputStreamReader(this.inputStream, StandardCharsets.UTF_8);
                myWriter = new OutputStreamWriter(this.outputStream, StandardCharsets.UTF_8);

            } else {
                throw new ConnectException("通道打开失败");
            }

        } else {
            throw new ConnectException("连接超时");
        }
    }


    @Override
    public void close() {
        this.shellChannel.close(false);
    }

    @Override
    public void resize(TtySize ttySize) throws IOException {
        Assert.isTrue(this.isConnected(), "当前通道已关闭");
        shellChannel.sendWindowChange(ttySize.columns(), ttySize.lines(), ttySize.height(), ttySize.width());
    }

    @Override
    public void write(String string) throws IOException {
        myWriter.write(string);
        myWriter.flush();
    }

    @Override
    public int read(char[] chars) throws IOException {
        return myReader.read(chars);
    }


    @Override
    public boolean isConnected() {
        return shellChannel.isOpen();
    }

    @Override
    public boolean isInCommandInput() {
        return false;
    }


    @Override
    public TtyType getTtyOsType() {
        return null;
    }

    @Override
    public FileManager getFileManager() {
        return null;
    }

    @Override
    public String getSessionId() {
        return "";
    }


}
