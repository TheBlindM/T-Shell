package com.tshell.core;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.NoSuchFileException;
import java.util.List;
import java.util.function.Consumer;

/**
 * 文件管理器
 *
 * @author TheBlind
 */
public interface FileManager {

    /**
     * 创建文件
     *
     * @param dir  所在目录
     * @param name 文件名称
     * @param type 文件类型
     */
    void create(String dir,
                String name,
                FileType type);

    /**
     * 更新文件内容
     *
     * @param path    文件路径
     * @param content 内容
     */
    void updateContent(String path, String content);


    /**
     * 上传文件
     *
     * @param path     所在目录
     * @param consumer 处理OutputStream
     * @param offset   偏移量
     */
    void upload(String path, Consumer<OutputStream> consumer, long offset);

    /**
     * 移除文件
     *
     * @param path 文件路径
     */
    void removeFile(String path);

    /**
     * 移除目录
     *
     * @param dir 目录路径
     */
    void removeDir(String dir);


    /**
     * 重命名文件或者目录
     *
     * @param oldPath 原始路径
     * @param newName 新名称
     */
    void rename(String oldPath, String newName);



    /**
     * 读取文件
     *
     * @param path     文件路径
     * @param consumer 处理InputStream
     */
    void read(String path, Consumer<InputStream> consumer);

    /**
     * 读取文件
     *
     * @param path     文件路径
     * @param consumer 处理InputStream
     * @param offset   偏移量
     */
    void read(String path, Consumer<InputStream> consumer, long offset);

    /**
     * 获取文件大小
     *
     * @param path 文件路径
     * @return 文件大小
     */
    long getSize(String path);


    /**
     * 获取文件信息列表
     *
     * @param path 文件路径
     * @return 文件信息列表
     */
    List<FileInfo> fileInfos(String path);


    /**
     * 获取文件信息
     *
     * @param path 文件路径
     * @return 文件信息列表
     */
    FileInfo fileInfo(String path) ;


    /**
     * 返回名称分隔符，表示为字符串
     *
     * @return 分隔符
     */
    String getSeparator();

}
