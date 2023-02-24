package com.tshell.core.ssh.jsch;



import cn.hutool.core.lang.Assert;
import cn.hutool.extra.ssh.JschRuntimeException;
import com.tshell.utils.StrUtil;
import com.tshell.utils.io.IoUtil;
import com.jcraft.jsch.*;

import java.io.*;
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

    public static Session openSession(String host, int port, String user, String pwd, int timeout) {
        final Session session = createSession(host, port, user, pwd);
        try {
            session.connect(timeout);
        } catch (JSchException e) {
            System.out.println(e.getMessage());
        }
        return session;
    }

    public static Session openSession(String host, int port, String user, String privateKeyPath, String passphrase, int timeout) {
        final Session session = createSession(host, port, user, privateKeyPath,passphrase);
        try {
            session.connect(timeout);
        } catch (JSchException e) {
            System.out.println(e.getMessage());
        }
        return session;
    }



    public static Session createSession(String host, int port, String user, String pass) {
        final JSch jsch = new JSch();
        final Session session = createSession(jsch, host, port, user);
        session.setPassword(pass);
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
    public static Session createSession(String host, int port, String user, String privateKeyPath, String passphrase)  {
        final JSch jsch = new JSch();
        try {
            jsch.addIdentity(privateKeyPath, passphrase);
        } catch (JSchException e) {
            throw new RuntimeException(e);
        }
        return createSession(jsch, host, port, user);
    }



    public static Session createSession(JSch jSch, String host, int port, String user) {
        Session session = null;
        try {
            session = jSch.getSession(user, host, port);
        } catch (JSchException e) {
            e.printStackTrace();
        }
        // 设置第一次登录的时候提示，可选值：(ask | yes | no)
        session.setConfig("StrictHostKeyChecking", "no");
        return session;
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

            if (false == session.isConnected()) {
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

    public static void executeCmd(Session session, String cmd, Charset charset, Consumer<InputStream> consumer,Consumer<InputStream> errConsumer) throws IOException, JSchException {
        if (null == charset) {
            charset = StandardCharsets.UTF_8;
        }

        ChannelExec channel = (ChannelExec) createChannel(session, ChannelType.EXEC);
        channel.setCommand(StrUtil.bytes(cmd, charset));
        channel.setInputStream((InputStream)null);
        InputStream errStream = channel.getErrStream();
        InputStream in = null;

        try {
            channel.connect();
            in = channel.getInputStream();
            consumer.accept(in);
            errConsumer.accept(errStream);
        }  finally {
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
        return sftpOperate(session,function,true);
    }

    public static <T> T sftpOperate(Session session, Function<ChannelSftp, T> function,boolean autoClose) {
        ChannelSftp sftp = null;
        try{
            sftp = createSftp(session);
            sftp.connect();
            sftp.setFilenameEncoding(StandardCharsets.UTF_8.toString());
            return function.apply(sftp);
        } catch (JSchException | SftpException e) {
            throw new RuntimeException(e);
        } finally {
            if (autoClose){
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

    private static final class PumpThread extends Thread {
        private final InputStream in;
        private final OutputStream out;

        public PumpThread(InputStream in, OutputStream out) {
            super("pump thread");
            this.in = in;
            this.out = out;
        }

        @Override
        public void run() {
            byte[] buf = new byte[1024];
            try {
                while (true) {
                    int len = in.read(buf);
                    if (len < 0) {
                        in.close();
                        return;
                    }
                    out.write(buf, 0, len);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
