package com.tshell.core.client;

import com.tshell.core.FileInfo;
import com.tshell.core.FileType;

import java.util.List;
import java.util.function.Function;

/**
 *
 * 客户端处理者
 * 用于应对 不同TTY
 * @author TheBlind
 */
public abstract class ClientHandler {

    public  final TtyType TtyType;

    public ClientHandler(TtyType ttyType) {
        TtyType = ttyType;
    }

    /**
     * 传入文本是否处在命令行
     * @param rowText 行文本
     * @return 是否处在命令行
     */
    public abstract boolean isInCommandInput(String rowText);

    /**
     * 当前客户端的分隔符
     * @return 分隔符
     */
    public abstract String  getSeparator();

    /**
     * 传入路径下的文件休息列表
     * @param exec 执行器
     * @param completePath 路径
     * @return fileInfo列表
     */
    public abstract List<FileInfo> getFileInfos(Function<String, String> exec, String completePath);

    /**
     * @return ttyType
     */
    public TtyType getTtyOsType(){
        return TtyType;
    }

    /**
     * 创建文件
     * @param exec 执行器
     * @param completePath 路径
     * @param fileType 文件类型
     */
    public abstract void createFile(Function<String, String> exec, String completePath, FileType fileType);

}
