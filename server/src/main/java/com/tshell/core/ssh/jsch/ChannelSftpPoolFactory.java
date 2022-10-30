package com.tshell.core.ssh.jsch;

import com.jcraft.jsch.ChannelSftp;
import org.apache.commons.pool2.impl.GenericKeyedObjectPoolConfig;

/**
 * @author TheBlind
 */
public class ChannelSftpPoolFactory {

    public static   ChannelSftpPool objectPool(){
        var factory = new ChannelSftpFactory();
        var config = new GenericKeyedObjectPoolConfig<ChannelSftp>();
        //链接池中最大连接数,默认为8
        config.setMaxTotalPerKey(6);
        //链接池中最大空闲的连接数,默认也为8
        config.setMaxIdlePerKey(2);
        //连接池中最少空闲的连接数,默认为0
        config.setMinIdlePerKey(0);

        //当这个值为true的时候，maxWaitMillis参数才能生效。为false的时候，当连接池没资源，则立马抛异常。默认为true
        config.setBlockWhenExhausted(true);

        //默认false，borrow的时候检测是有有效，如果无效则从连接池中移除，并尝试继续获取
        config.setTestOnBorrow(true);
        //默认false，return的时候检测是有有效，如果无效则从连接池中移除，并尝试继续获取
        config.setTestOnReturn(true);

        //默认false，在evictor线程里头，当evictionPolicy.evict方法返回false时，而且testWhileIdle为true的时候则检测是否有效，如果无效则移除
        config.setTestWhileIdle(true);
        config.setJmxEnabled(false);

        //空闲连接被驱逐前能够保留的时间
        config.setMinEvictableIdleTimeMillis(10000L);
        //当空闲线程大于minIdle 空闲连接能够保留时间，同时指定会被上面的覆盖
        config.setSoftMinEvictableIdleTimeMillis(10000L);
        //驱逐线程执行间隔时间
        config.setTimeBetweenEvictionRunsMillis(200000L);

        return new ChannelSftpPool(factory,config);
    }






}
