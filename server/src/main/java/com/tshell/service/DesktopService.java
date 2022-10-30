package com.tshell.service;

import cn.hutool.core.io.FileUtil;

import javax.enterprise.context.ApplicationScoped;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.URI;

/**
 * @author TheBlind
 * @version 1.0
 */
@ApplicationScoped
public class DesktopService {
    final String githubUrl = "https://github.com/TheBlindM/T-Shell";

    /**
     * 前往github 上开源地址
     *
     * @throws IOException
     */
    public void toGithub() throws IOException {
        URI uri = URI.create(githubUrl);
        Desktop.getDesktop().browse(uri);
    }

    /**
     * 利用系统默认 文件资源管理器 打开文件
     * @param path 文件路径
     * @throws IOException
     */
    public void open(String path) throws IOException {
        Desktop.getDesktop().open(FileUtil.file(path));
    }


}
