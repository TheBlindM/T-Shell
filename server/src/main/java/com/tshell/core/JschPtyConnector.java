package com.tshell.core;

import cn.hutool.core.date.TimeInterval;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import com.jcraft.jsch.*;
import com.tshell.core.client.ClientFactory;
import com.tshell.core.client.ClientHandler;
import com.tshell.core.client.TtyType;
import com.tshell.core.ssh.jsch.ChannelSftpPool;
import com.tshell.core.ssh.jsch.ChannelSftpPoolFactory;
import com.tshell.core.ssh.jsch.JschUtil;
import com.tshell.core.tty.TtyConnector;
import com.tshell.module.enums.ProxyType;
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

    private final Parameter.SshParameter sshParameter;

    public JschPtyConnector(Parameter.SshParameter parameter) throws IOException {
        Session session = null;
        ChannelShell channel = null;
        try {
            session = createSession(parameter);
            channel = JschUtil.createShell(session);
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
        this.sshParameter = parameter;
    }

    private static void handleConnectException(String message) throws SocketException {
        if (message.endsWith("Network is unreachable: connect")) {
            throw new SocketException("网络连接失败");
        } else if (message.endsWith("Connection refused: connect") || message.endsWith("Packet corrupt") || message.endsWith("Auth fail")) {
            throw new ConnectException("连接被拒绝,请检查信息是否正确");
        }
    }

    private Session createSession(Parameter.SshParameter parameter) {
        Proxy proxy = switch (parameter.getProxyType()) {
            case HTTP -> new ProxyHTTP(parameter.proxyHost, parameter.proxyPort);
            case SOCKET -> new ProxySOCKS5(parameter.proxyHost, parameter.proxyPort);
            case DIRECT -> null;
        };

        Session session = switch (parameter.authType) {
            case PWD, KEYBOARD_INTERACTIVE ->
                    JschUtil.openSession(parameter.getIp(), parameter.getPort(), parameter.getUsername(), parameter.getPwd(), 30000, proxy);
            case PUBLIC_KEY ->
                    JschUtil.openSession(parameter.getIp(), parameter.getPort(), parameter.getUsername(), parameter.getPrivateKeyFile(), parameter.getPassphrase(), 30000, proxy);
        };
        return session;
    }


    @Override
    public String getSessionId() {
        return this.sessionId;
    }

    @Override
    public void close() {
        if (this.isConnected()) {
            this.shellChannel.disconnect();
        }
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
                return CHANNEL_SFTP_POOL.borrowObject(sshParameter);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public void create(String dir, String name, FileType type) {
            // todo 优化传参 直接传一个完整路径
            String separator = clientHandler.getSeparator();
            if (dir == null || "null".equals(dir)) {
                dir = separator;
            }
            String newName = name;
            if (!newName.startsWith(separator)) {
                newName = separator + name;
            }
            String completePath = dir.endsWith(separator) ? dir : dir + newName;

            clientHandler.createFile((cmd) -> {
                try {
                    return cn.hutool.extra.ssh.JschUtil.exec(shellChannel.getSession(), cmd, StandardCharsets.UTF_8);
                } catch (JSchException e) {
                    throw new RuntimeException(e);
                }
            }, completePath, type);
        }

        @Override
        public void updateContent(String writePath, String readPath) {
            ChannelSftp sftpChannel = getSftpChannel();
            try (BufferedInputStream inputStream = FileUtil.getInputStream(new File(readPath))) {
                sftpChannel.put(inputStream, writePath, ChannelSftp.OVERWRITE);
            } catch (SftpException | IOException e) {
                throw new RuntimeException(e);
            } finally {
                CHANNEL_SFTP_POOL.returnObject(sshParameter, sftpChannel);
            }
        }


        @Override
        public void upload(String path, Consumer<OutputStream> consumer, long offset) {
            String separator = clientHandler.getSeparator();
            if (path == null) {
                path = separator;
            }

            String completePath = path;
            ChannelSftp sftpChannel = getSftpChannel();
            TimeInterval timer = cn.hutool.core.date.DateUtil.timer();
            try {
                consumer.accept(sftpChannel.put(completePath, null, ChannelSftp.APPEND, offset));
            } catch (SftpException e) {
                throw new RuntimeException(e);
            } finally {
                CHANNEL_SFTP_POOL.returnObject(sshParameter, sftpChannel);
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
                CHANNEL_SFTP_POOL.returnObject(sshParameter, sftpChannel);
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
                CHANNEL_SFTP_POOL.returnObject(sshParameter, sftpChannel);
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
                CHANNEL_SFTP_POOL.returnObject(sshParameter, sftpChannel);
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
                CHANNEL_SFTP_POOL.returnObject(sshParameter, sftpChannel);
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
                CHANNEL_SFTP_POOL.returnObject(sshParameter, sftpChannel);
            }
        }

        @Override
        public List<FileInfo> fileInfos(String path) {
            return clientHandler.getFileInfos((cmd) -> {
                try {
                    return cn.hutool.extra.ssh.JschUtil.exec(shellChannel.getSession(), cmd, StandardCharsets.UTF_8);
                } catch (JSchException e) {
                    throw new RuntimeException(e);
                }
            }, path);
        }

        /**
         * 获取文件信息
         *
         * @param path 文件路径
         * @return 文件信息列表
         */
        @Override
        public FileInfo fileInfo(String path) {
            return fileInfos(path).stream().findFirst().orElseThrow();
        }

        /**
         * 返回名称分隔符，表示为字符串
         *
         * @return 分隔符
         */
        @Override
        public String getSeparator() {
            return clientHandler.getSeparator();
        }

    }


}
