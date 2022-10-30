package com.tshell.core.ssh;



import com.tshell.module.entity.SshSession;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * @program: WSell
 * @description:
 * @author: TheBlind
 * @create: 2021-01-29 15:21
 **/
public interface WebShellService {


    /**
     * 初始化 连接
     *
     * @param sshSession 主机
     * @param id   一个 socket 前端会生成uuid + hostid
     */
    void initialConnection(SshSession sshSession, String id);


    /**
     * 关闭
     */
    void close(String id);

    boolean uploadFile(String id,String targetPath, String localFilePath);

    boolean uploadFile(String id,String targetPath, InputStream localFileInput);

    boolean downloadFile(String id,String remotePath,String localPath);

    /**
     * 执行操作
     * @param cmd 命令
     * @param id  sessionId
     * @param resultOutput
     * @param errOutput
     * @param continueOutput
     */
    void executeCmd(String cmd, String id, OutputStream resultOutput,OutputStream errOutput, OutputStream continueOutput);
    void executeShell(String id, OutputStream resultOutput,OutputStream errOutput, OutputStream continueOutput);

    /**
     * 执行玩的回调
     *
     * @param id
     */
    void callback(String id, String message, boolean isErr);

}
