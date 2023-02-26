package com.tshell.core.ssh.jsch;

import com.tshell.core.Parameter;
import com.tshell.core.ssh.SshConfig;
import com.jcraft.jsch.ChannelSftp;
import org.apache.commons.pool2.KeyedPooledObjectFactory;
import org.apache.commons.pool2.impl.GenericKeyedObjectPool;
import org.apache.commons.pool2.impl.GenericKeyedObjectPoolConfig;

/**
 * @author TheBlind
 */
public class ChannelSftpPool extends GenericKeyedObjectPool<Parameter.SshParameter, ChannelSftp> {


    public ChannelSftpPool(KeyedPooledObjectFactory<Parameter.SshParameter, ChannelSftp> factory) {
        super(factory);
    }

    public ChannelSftpPool(KeyedPooledObjectFactory<Parameter.SshParameter, ChannelSftp> factory, GenericKeyedObjectPoolConfig<ChannelSftp> config) {
        super(factory, config);
    }
}
