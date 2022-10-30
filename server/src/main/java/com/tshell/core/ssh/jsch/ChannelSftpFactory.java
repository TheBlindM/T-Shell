package com.tshell.core.ssh.jsch;

import com.tshell.core.ssh.SshConfig;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.pool2.DestroyMode;
import org.apache.commons.pool2.KeyedPooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;

/**
 *
 * https://www.junpzx.cn/archives/customsize-sftp#shellfactory
 * @author TheBlind
 */
@Slf4j
public class ChannelSftpFactory implements KeyedPooledObjectFactory<SshConfig, ChannelSftp> {


    @Override
    public void destroyObject(SshConfig key, PooledObject<ChannelSftp> p, DestroyMode destroyMode) throws Exception {
        KeyedPooledObjectFactory.super.destroyObject(key, p, destroyMode);
    }

    @Override
    public void activateObject(SshConfig sshParameter, PooledObject<ChannelSftp> pooledObject)  {

    }

    @Override
    public void destroyObject(SshConfig sshParameter, PooledObject<ChannelSftp> pooledObject)  {
        log.error("销毁对象{}",sshParameter);
        final ChannelSftp channelSftp = pooledObject.getObject();
        channelSftp.disconnect();
    }

    @Override
    public PooledObject<ChannelSftp> makeObject(SshConfig parameter)  {
        Session session = JschUtil.openSession(parameter.ip(), parameter.port(), parameter.username(), parameter.pwd(), 3000);
        try {
            log.error("创建对象{}",parameter);
            ChannelSftp channel = (ChannelSftp) session.openChannel("sftp");
            channel.connect();
            return new DefaultPooledObject<>(channel) ;
        } catch (JSchException e) {
            throw new RuntimeException(e);
        }

    }



    @Override
    public void passivateObject(SshConfig sshParameter, PooledObject<ChannelSftp> pooledObject) {
    }

    @Override
    public boolean validateObject(SshConfig sshParameter, PooledObject<ChannelSftp> pooledObject) {
        log.error("验证对象{}",sshParameter);
        final ChannelSftp channelSftp = pooledObject.getObject();
        try {
            if (channelSftp.isClosed()) {
                return false;
            }
            channelSftp.cd("/");
        } catch (SftpException e) {
            return false;
        }
        return true;
    }
}
