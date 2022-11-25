package com.tshell.core.local;

import com.tshell.core.FileManager;
import com.tshell.core.TtySize;
import com.tshell.core.client.TtyType;
import com.tshell.core.tty.TtyConnector;

import java.io.IOException;

/**
 *
 * 本地终端
 * @author TheBlind
 * @version 1.0
 */
public class LocalTtyConnector implements TtyConnector {
    /**
     * 获取sessionId
     *
     * @return sessionId
     */
    @Override
    public String getSessionId() {
        return null;
    }

    /**
     * 关闭当前连接
     */
    @Override
    public void close() {

    }

    /**
     * 设置终端size
     *
     * @param ttySize size
     * @throws IOException
     */
    @Override
    public void resize(TtySize ttySize) throws IOException {

    }

    /**
     * 写入一个字符串
     *
     * @param string 要写入的字符串
     * @throws IOException
     */
    @Override
    public void write(String string) throws IOException {

    }

    /**
     * 将字符读入数组。此方法将阻塞，直到某些输入可用、发生 I/O 错误或到达流的末尾。
     *
     * @param chars 目标缓冲区
     * @return 读取的字符数，如果已到达流的末尾，则为 -1
     * @throws IOException
     */
    @Override
    public int read(char[] chars) throws IOException {
        return 0;
    }

    /**
     * 连接状态
     *
     * @return 连接状态
     */
    @Override
    public boolean isConnected() {
        return false;
    }

    /**
     * 是否在输入命令中
     *
     * @return 是否在输入命令中
     */
    @Override
    public boolean isInCommandInput() {
        return false;
    }

    /**
     * 获取终端类型
     *
     * @return 终端类型
     */
    @Override
    public TtyType getTtyOsType() {
        return null;
    }

    /**
     * 获取文件管理器
     *
     * @return 文件管理器
     */
    @Override
    public FileManager getFileManager() {
        return null;
    }
}
