package com.tshell.core;

import cn.hutool.core.date.TimeInterval;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import com.tshell.core.client.ClientFactory;
import com.tshell.core.client.ClientHandler;
import com.tshell.core.client.TtyType;
import com.tshell.core.ssh.SshConfig;
import com.tshell.core.ssh.jsch.ChannelSftpPool;
import com.tshell.core.ssh.jsch.ChannelSftpPoolFactory;
import com.tshell.core.ssh.jsch.JschSessionPoll;
import com.tshell.core.ssh.jsch.JschUtil;
import com.tshell.core.tty.TtyConnector;
import com.jcraft.jsch.*;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.net.ConnectException;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

/**
 * @author TheBlind
 * @date 2022/7/21
 */
@Slf4j
public final class JschPtyConnector implements TtyConnector {

    final static ChannelSftpPool CHANNEL_SFTP_POOL = ChannelSftpPoolFactory.objectPool();
    private final Reader myReader;
    private final Writer myWriter;
    private final ChannelShell shellChannel;
    private final String sessionId;
    ClientHandler clientHandler;
    StringBuilder originalLineText = new StringBuilder();

    private final FileManager fileManager;


    private AtomicBoolean inCommandInput = new AtomicBoolean(false);

    private final SshConfig sshConfig;

    public JschPtyConnector(Parameter.SshParameter parameter) throws IOException {
        Session session = getSession(parameter);
        ChannelShell channel = null;
        try {
            channel = (ChannelShell) JschUtil.createShell(session);
        } catch (JSchException e) {
            handleConnectException(e.getMessage());
            throw new RuntimeException(e);
        }
        TtySize ttySize = parameter.getTtySize();
        // todo 后期考虑由配置
        channel.setPtyType("xterm-256color");
        channel.setPtySize(ttySize.columns(), ttySize.lines(), ttySize.width(), ttySize.height());

        try {
            session.setServerAliveInterval(10000);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        try {
            channel.connect();
        } catch (JSchException e) {
            throw new RuntimeException(e);
        }
        shellChannel = channel;

        this.myWriter = new OutputStreamWriter(channel.getOutputStream(), StandardCharsets.UTF_8);
        this.myReader = new InputStreamReader(channel.getInputStream(), StandardCharsets.UTF_8);
        this.clientHandler = ClientFactory.getClient(parameter.getTtyTypeId());
        this.sessionId = parameter.getSessionId();

        this.fileManager = new MyFileManager();
        this.sshConfig = new SshConfig(parameter.ip, parameter.port, parameter.username, parameter.pwd);
    }

    private static void handleConnectException(String message) throws SocketException {
        if (message.endsWith("Network is unreachable: connect")) {
            throw new SocketException("网络连接失败");
        } else if (message.endsWith("Connection refused: connect") || message.endsWith("Packet corrupt")) {
            throw new ConnectException("连接被拒绝,请检查信息是否正确");
        }
    }

    private Session getSession(Parameter.SshParameter parameter) {
        return getSession(parameter, false);
    }

    private Session getSession(Parameter.SshParameter parameter, boolean create) {
        Session session = JschSessionPoll.INSTANCE.getSession(parameter.getSessionId(), parameter.getIp(), parameter.getPort(), parameter.getUsername(), parameter.getPwd(), 3000, create);
        return session;
    }


    @Override
    public String getSessionId() {
        return this.sessionId;
    }

    @Override
    public void close() {
        this.shellChannel.disconnect();
    }

    @Override
    public void resize(TtySize ttySize) {
        Assert.isTrue(this.isConnected(), "当前通道已关闭");
        shellChannel.setPtySize(ttySize.columns(), ttySize.lines(), ttySize.height(), ttySize.width());
    }

    @Override
    public void write(String string) throws IOException {
        myWriter.write(string);
        myWriter.flush();
    }

    @Override
    public int read(char[] chars) throws IOException {
        int count = myReader.read(chars);
        if (count == -1) {
            return -1;
        }
        String outText = String.valueOf(chars, 0, count);
        if (outText.contains(StrUtil.CRLF)) {
            originalLineText = new StringBuilder(outText);
        } else {
            originalLineText.append(outText);
        }
        return count;
    }


    @Override
    public boolean isConnected() {
        return shellChannel.isConnected();
    }

    @Override
    public boolean isInCommandInput() {
        return clientHandler.isInCommandInput(originalLineText.toString());
    }


    @Override
    public TtyType getTtyOsType() {
        return clientHandler.getTtyOsType();
    }


    @Override
    public FileManager getFileManager() {
        return this.fileManager;
    }

    class MyFileManager implements FileManager {


        private ChannelSftp getSftpChannel() {
            try {
                return CHANNEL_SFTP_POOL.borrowObject(sshConfig);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public void create(String dir, String name, FileType type) {
            String separator = clientHandler.getSeparator();
            if (dir == null || "null".equals(dir)) {
                dir = separator;
            }
            String completePath = dir.endsWith(separator) ? dir : dir + separator + name;

            clientHandler.createFile((cmd) -> {
                return cn.hutool.extra.ssh.JschUtil.exec(JschSessionPoll.INSTANCE.getSession(sessionId), cmd, StandardCharsets.UTF_8);
            }, completePath, type);
        }

        @Override
        public void updateContent(String path, String content) {
            ChannelSftp sftpChannel = getSftpChannel();
            try {
                sftpChannel.put(new ByteArrayInputStream(content.getBytes()), path, ChannelSftp.OVERWRITE);
            } catch (SftpException e) {
                throw new RuntimeException(e);
            } finally {
                CHANNEL_SFTP_POOL.returnObject(sshConfig, sftpChannel);
            }
        }


        @Override
        public void upload(String dir, String fileName, Consumer<OutputStream> consumer, long offset) {
            String separator = clientHandler.getSeparator();
            if (dir == null) {
                dir = separator;
            }

            String completePath = dir.endsWith(separator) ? dir + fileName : dir + separator + fileName;
            ChannelSftp sftpChannel = getSftpChannel();
            TimeInterval timer = cn.hutool.core.date.DateUtil.timer();
            try {
                consumer.accept(sftpChannel.put(completePath, null, ChannelSftp.APPEND, offset));
            } catch (SftpException e) {
                throw new RuntimeException(e);
            } finally {
                CHANNEL_SFTP_POOL.returnObject(sshConfig, sftpChannel);
            }
            long interval = timer.interval();
            log.debug("耗时" + interval);
        }

        @Override
        public void removeFile(String path) {
            ChannelSftp sftpChannel = getSftpChannel();
            try {
                sftpChannel.rm(path);
            } catch (SftpException e) {
                throw new RuntimeException(e);
            } finally {
                CHANNEL_SFTP_POOL.returnObject(sshConfig, sftpChannel);
            }
        }

        @Override
        public void removeDir(String dir) {
            ChannelSftp sftpChannel = getSftpChannel();
            try {
                removeDirectory0(sftpChannel, dir);
            } catch (SftpException e) {
                throw new RuntimeException(e);
            } finally {
                CHANNEL_SFTP_POOL.returnObject(sshConfig, sftpChannel);
            }
        }

        public void removeDirectory0(ChannelSftp sftpChannel, String path) throws SftpException {
            sftpChannel.cd(path);
            Vector<ChannelSftp.LsEntry> entries = sftpChannel.ls(sftpChannel.pwd());
            Iterator var4 = entries.iterator();

            while (var4.hasNext()) {
                ChannelSftp.LsEntry entry = (ChannelSftp.LsEntry) var4.next();
                String fileName = entry.getFilename();
                if (!".".equals(fileName) && !"..".equals(fileName)) {
                    if (entry.getAttrs().isDir()) {
                        removeDirectory0(sftpChannel, fileName);
                    } else {
                        sftpChannel.rm(fileName);
                    }
                }
            }
            sftpChannel.cd("..");
            sftpChannel.rmdir(path);
        }

        @Override
        public void rename(String oldPath, String newName) {
            ChannelSftp sftpChannel = getSftpChannel();
            try {
                String oldName = FileUtil.getName(oldPath);
                String newPath = oldPath.replace(oldName, newName);
                sftpChannel.rename(oldPath, newPath);
            } catch (SftpException e) {
                throw new RuntimeException(e);
            } finally {
                CHANNEL_SFTP_POOL.returnObject(sshConfig, sftpChannel);
            }
        }

        @Override
        public void read(String path, Consumer<InputStream> consumer) {
            read(path, consumer, 0);
        }

        @Override
        public void read(String path, Consumer<InputStream> consumer, long offset) {
            ChannelSftp sftpChannel = getSftpChannel();
            try {
                consumer.accept(sftpChannel.get(path, null, offset));
            } catch (SftpException e) {
                throw new RuntimeException(e);
            } finally {
                CHANNEL_SFTP_POOL.returnObject(sshConfig, sftpChannel);
            }
        }

        @Override
        public long getSize(String path) {
            ChannelSftp sftpChannel = getSftpChannel();
            try {
                SftpATTRS sftpATTRS = sftpChannel.lstat(path);
                return sftpATTRS.getSize();
            } catch (SftpException e) {
                throw new RuntimeException(e);
            } finally {
                CHANNEL_SFTP_POOL.returnObject(sshConfig, sftpChannel);
            }
        }

        @Override
        public List<FileInfo> fileInfos(String path) {
            String separator = clientHandler.getSeparator();
            if (path == null || "null".equals(path)) {
                path = separator;
            }
            String completePath = path.endsWith(separator) ? path : path + separator;
            return clientHandler.getFileInfos((cmd) -> {
                try {
                    return cn.hutool.extra.ssh.JschUtil.exec(shellChannel.getSession(), cmd, StandardCharsets.UTF_8);
                } catch (JSchException e) {
                    throw new RuntimeException(e);
                }
            }, completePath);
        }
    }


}
