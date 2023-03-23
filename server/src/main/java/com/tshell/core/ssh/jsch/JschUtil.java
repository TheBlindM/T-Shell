package com.tshell.core.ssh.jsch;


import cn.hutool.core.lang.Assert;
import cn.hutool.core.net.LocalPortGenerater;
import cn.hutool.extra.ssh.JschRuntimeException;
import com.jcraft.jsch.*;
import com.tshell.utils.StrUtil;
import com.tshell.utils.io.IoUtil;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * 多个channel 不能同时发送命令
 *
 * @author TheBlind
 * @date 2022/4/2
 */


public class JschUtil {

    /**
     * 本地端口生成器
     */
    private static final LocalPortGenerater portGenerater = new LocalPortGenerater(10000);

    /**
     * 生成一个本地端口，用于远程端口映射
     *
     * @return 未被使用的本地端口
     */
    public static int generateLocalPort() {
        return portGenerater.generate();
    }

    public static Session openSession(String host, int port, String user, String pwd, int timeout) {
        final Session session = createSession(host, port, user, pwd);
        try {
            session.connect(timeout);
        } catch (JSchException e) {
            throw new RuntimeException(e);
        }
        return session;
    }

    public static Session openSession(String host, int port, String user, String privateKeyPath, String passphrase, int timeout) {
        final Session session;
        try {
            session = createSession(host, port, user, privateKeyPath, passphrase);
            session.connect(timeout);
        } catch (JSchException e) {
            throw new RuntimeException(e);
        }
        return session;
    }


    public static Session createSession(String host, int port, String user, String pass) {
        final JSch jsch = new JSch();
        final Session session;
        try {
            session = createSession(jsch, host, port, user);
            if (!StrUtil.isEmpty(pass)) {
                session.setPassword(pass);
            }
        } catch (JSchException e) {
            throw new RuntimeException(e);
        }
        return session;
    }

    /**
     * 新建一个新的SSH会话，此方法并不打开会话（既不调用connect方法）
     *
     * @param host           主机
     * @param port           端口
     * @param user           用户名，如果为null，默认root
     * @param privateKeyPath 私钥的路径
     * @param passphrase     私钥文件的密码，可以为null
     * @return SSH会话
     * @since 5.0.0
     */
    public static Session createSession(String host, int port, String user, String privateKeyPath, String passphrase) throws JSchException {
        final JSch jsch = new JSch();
        jsch.addIdentity(privateKeyPath, passphrase);
        return createSession(jsch, host, port, user);
    }


    public static Session createSession(JSch jsch, String host, int port, String user) throws JSchException {
        Assert.notEmpty(host, "SSH Host must be not empty!");
        Assert.isTrue(port > 0, "SSH port must be > 0");

        // 默认root用户
        if (cn.hutool.core.util.StrUtil.isEmpty(user)) {
            user = "root";
        }

        if (null == jsch) {
            jsch = new JSch();
        }

        Session session;
        session = jsch.getSession(user, host, port);
        // 设置第一次登录的时候提示，可选值：(ask | yes | no)
        session.setConfig("StrictHostKeyChecking", "no");
        return session;
    }


    /**
     * 绑定端口到本地。 一个会话可绑定多个端口
     *
     * @param session    需要绑定端口的SSH会话
     * @param remoteHost 远程主机
     * @param remotePort 远程端口
     * @param localPort  本地端口
     * @return 成功与否
     * @throws JschRuntimeException 端口绑定失败异常
     */
    public static boolean bindPort(Session session, String remoteHost, int remotePort, int localPort){
        return bindPort(session, remoteHost, remotePort, "127.0.0.1", localPort);
    }

    /**
     * 绑定端口到本地。 一个会话可绑定多个端口
     *
     * @param session    需要绑定端口的SSH会话
     * @param remoteHost 远程主机
     * @param remotePort 远程端口
     * @param localHost  本地主机
     * @param localPort  本地端口
     * @return 成功与否
     */
    public static boolean bindPort(Session session, String remoteHost, int remotePort, String localHost, int localPort) {
        if (session != null && session.isConnected()) {
            try {
                session.setPortForwardingL(localHost, localPort, remoteHost, remotePort);
            } catch (JSchException e) {
                throw new JschRuntimeException(e, "From [{}:{}] mapping to [{}:{}] error！", remoteHost, remotePort, localHost, localPort);
            }
            return true;
        }
        return false;
    }


    /**
     * 绑定ssh服务端的serverPort端口, 到host主机的port端口上. <br>
     * 即数据从ssh服务端的serverPort端口, 流经ssh客户端, 达到host:port上.
     *
     * @param session  与ssh服务端建立的会话
     * @param bindPort ssh服务端上要被绑定的端口
     * @param host     转发到的host
     * @param port     host上的端口
     * @return 成功与否
     */
    public static boolean bindRemotePort(Session session, int bindPort, String host, int port) throws JschRuntimeException {
        if (session != null && session.isConnected()) {
            try {
                session.setPortForwardingR(bindPort, host, port);
            } catch (JSchException e) {
                throw new JschRuntimeException(e, "From [{}] mapping to [{}] error！", bindPort, port);
            }
            return true;
        }
        return false;
    }


    /**
     * https://www.jianshu.com/p/ede91b0b8495
     * <p>
     * sftp
     * https://www.cnblogs.com/longyg/archive/2012/06/25/2556576.html
     * <p>
     * 功能
     * https://www.cnblogs.com/dongliyang/p/4173583.html
     * <p>
     * ChannelSftp类是JSch实现SFTP核心类，它包含了所有SFTP的方法，如：
     * put()：      文件上传
     * get()：      文件下载
     * cd()：       进入指定目录
     * ls()：       得到指定目录下的文件列表
     * rename()：   重命名指定文件或目录
     * rm()：       删除指定文件
     * mkdir()：    创建目录
     * rmdir()：    删除目录
     * <p>
     * OVERWRITE   完全覆盖模式，这是JSch的默认文件传输模式，即如果目标文件已经存在，传输的文件将完全覆盖目标文件，产生新的文件。
     * RESUME 恢复模式，如果文件已经传输一部分，这时由于网络或其他任何原因导致文件传输中断，如果下一次传输相同的文件，则会从上一次中断的地方续传。
     * APPEND 追加模式，如果目标文件已存在，传输的文件将在目标文件后追加。
     *
     * @param sessionId
     * @param targetPath
     * @param localFilePath
     * @return
     */

    /**
     * 创建Channel连接
     *
     * @param session     Session会话
     * @param channelType 通道类型，可以是shell或sftp等，见{@link ChannelType}
     * @return {@link Channel}
     * @since 4.5.2
     */
    public static Channel createChannel(Session session, ChannelType channelType) throws JSchException {
        Channel channel;
        if (!session.isConnected()) {
            session.connect();
        }
        channel = session.openChannel(channelType.getValue());
        return channel;
    }


    public static void executeCmd(Session session, String cmd, OutputStream resultOutput, OutputStream errOutput) throws Exception {

        //开启shell通道
        ChannelExec channel = (ChannelExec) createChannel(session, ChannelType.EXEC);
        try {
            //通道连接 超时时间3s
            channel.setCommand(cmd);
            InputStream inputStream = channel.getInputStream();
            channel.setErrStream(errOutput);
            channel.connect();
            inputStream.transferTo(resultOutput);
        } finally {
            channel.disconnect();
        }


    }

    public static void executeCmd(Session session, String cmd, Charset charset, Consumer<InputStream> consumer, Consumer<InputStream> errConsumer) throws IOException, JSchException {
        if (null == charset) {
            charset = StandardCharsets.UTF_8;
        }

        ChannelExec channel = (ChannelExec) createChannel(session, ChannelType.EXEC);
        channel.setCommand(StrUtil.bytes(cmd, charset));
        channel.setInputStream((InputStream) null);
        InputStream errStream = channel.getErrStream();
        InputStream in = null;

        try {
            channel.connect();
            in = channel.getInputStream();
            consumer.accept(in);
            errConsumer.accept(errStream);
        } finally {
            IoUtil.close(in);
            channel.disconnect();
        }
    }


    public static String executeCmd(Session session, String cmd, OutputStream errOutput) throws Exception {

        //开启shell通道
        ChannelExec channel = (ChannelExec) createChannel(session, ChannelType.EXEC);
        try {
            //通道连接 超时时间3s
            channel.setCommand(cmd);
            InputStream inputStream = channel.getInputStream();
            channel.setErrStream(errOutput);
            channel.connect();
            return IoUtil.readUtf8(inputStream);
        } finally {
            channel.disconnect();
        }


    }

    public static ChannelShell createShell(Session session, OutputStream resultOutput, InputStream cmdInput) throws JSchException, IOException {
        ChannelShell channel = (ChannelShell) createChannel(session, ChannelType.SHELL);
        channel.setPty(true);
        channel.setAgentForwarding(true);
        channel.setInputStream(cmdInput);
        channel.setOutputStream(resultOutput);
        channel.setPtyType("vt102");
        OutputStream outputStream = channel.getOutputStream();
        channel.connect();
        return channel;
    }


    public static ChannelShell createShell(Session session) throws JSchException {
        ChannelShell channel = (ChannelShell) createChannel(session, ChannelType.SHELL);
        return channel;
    }

    public static <T> T sftpOperate(Session session, Function<ChannelSftp, T> function) {
        return sftpOperate(session, function, true);
    }

    public static <T> T sftpOperate(Session session, Function<ChannelSftp, T> function, boolean autoClose) {
        ChannelSftp sftp = null;
        try {
            sftp = createSftp(session);
            sftp.connect();
            sftp.setFilenameEncoding(StandardCharsets.UTF_8.toString());
            return function.apply(sftp);
        } catch (JSchException | SftpException e) {
            throw new RuntimeException(e);
        } finally {
            if (autoClose) {
                closeChannel(sftp);
            }
        }
    }

    public static void closeChannel(Channel channel) {
        if (channel != null && channel.isConnected()) {
            channel.disconnect();
        }
    }

    public static ChannelSftp createSftp(Session session) throws JSchException {
        return (ChannelSftp) createChannel(session, ChannelType.SFTP);
    }

}
