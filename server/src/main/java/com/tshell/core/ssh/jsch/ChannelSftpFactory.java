package com.tshell.core.ssh.jsch;

import com.tshell.core.Parameter;
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
public class ChannelSftpFactory implements KeyedPooledObjectFactory<Parameter.SshParameter, ChannelSftp> {


    @Override
    public void destroyObject(Parameter.SshParameter key, PooledObject<ChannelSftp> p, DestroyMode destroyMode) throws Exception {
        KeyedPooledObjectFactory.super.destroyObject(key, p, destroyMode);
    }

    @Override
    public void activateObject(Parameter.SshParameter sshParameter, PooledObject<ChannelSftp> pooledObject)  {

    }

    @Override
    public void destroyObject(Parameter.SshParameter sshParameter, PooledObject<ChannelSftp> pooledObject)  {
        log.error("销毁对象{}",sshParameter);
        final ChannelSftp channelSftp = pooledObject.getObject();
        channelSftp.disconnect();
    }

    @Override
    public PooledObject<ChannelSftp> makeObject(Parameter.SshParameter parameter)  {
        Session session = createSession(parameter);
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
    public void passivateObject(Parameter.SshParameter sshParameter, PooledObject<ChannelSftp> pooledObject) {
    }

    @Override
    public boolean validateObject(Parameter.SshParameter sshParameter, PooledObject<ChannelSftp> pooledObject) {
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

    private Session createSession(Parameter.SshParameter parameter) {
        Session session=switch (parameter.getAuthType()){
            case PWD,KEYBOARD_INTERACTIVE-> JschUtil.openSession(parameter.getIp(), parameter.getPort(), parameter.getUsername(), parameter.getPwd(),3000);
            case PUBLIC_KEY -> JschUtil.openSession(parameter.getIp(), parameter.getPort(), parameter.getUsername(), parameter.getPrivateKeyFile(),parameter.getPassphrase(), 3000);
        };
        return session;
    }
}
