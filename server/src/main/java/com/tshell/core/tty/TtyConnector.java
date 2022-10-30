package com.tshell.core.tty;

import com.tshell.core.FileManager;
import com.tshell.core.TtySize;
import com.tshell.core.client.TtyType;

import java.io.IOException;

/**
 * @author TheBlind
 * @date 2022/7/21
 */
public interface TtyConnector {

    /**
     * 获取sessionId
     *
     * @return sessionId
     */
    String getSessionId();

    /**
     * 关闭当前连接
     */
    void close();

    /**
     * 设置终端size
     *
     * @param ttySize size
     * @throws IOException
     */
    void resize(TtySize ttySize) throws IOException;


    /**
     * 写入一个字符串
     *
     * @param string 要写入的字符串
     * @throws IOException
     */
    void write(String string) throws IOException;

    /**
     * 将字符读入数组。此方法将阻塞，直到某些输入可用、发生 I/O 错误或到达流的末尾。
     *
     * @param chars 目标缓冲区
     * @return 读取的字符数，如果已到达流的末尾，则为 -1
     * @throws IOException
     */
    int read(char[] chars) throws IOException;

    /**
     * 连接状态
     *
     * @return 连接状态
     */
    boolean isConnected();

    /**
     * 是否在输入命令中
     *
     * @return 是否在输入命令中
     */
    boolean isInCommandInput();


    /**
     * 获取终端类型
     *
     * @return 终端类型
     */
    TtyType getTtyOsType();

    /**
     * 获取文件管理器
     *
     * @return 文件管理器
     */
    FileManager getFileManager();


}
