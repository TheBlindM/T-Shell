package com.tshell.core.ssh.jsch;

import com.tshell.core.ssh.SshConfig;
import com.jcraft.jsch.ChannelSftp;
import org.apache.commons.pool2.KeyedPooledObjectFactory;
import org.apache.commons.pool2.impl.GenericKeyedObjectPool;
import org.apache.commons.pool2.impl.GenericKeyedObjectPoolConfig;

/**
 * @author TheBlind
 */
public class ChannelSftpPool extends GenericKeyedObjectPool<SshConfig, ChannelSftp> {


    public ChannelSftpPool(KeyedPooledObjectFactory<SshConfig, ChannelSftp> factory) {
        super(factory);
    }

    public ChannelSftpPool(KeyedPooledObjectFactory<SshConfig, ChannelSftp> factory, GenericKeyedObjectPoolConfig<ChannelSftp> config) {
        super(factory, config);
    }
}
