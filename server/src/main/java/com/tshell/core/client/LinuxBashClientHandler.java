package com.tshell.core.client;

import cn.hutool.core.util.StrUtil;
import com.tshell.core.FileInfo;
import com.tshell.core.FileType;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.tshell.core.FileType.FILE;

/**
 * @author TheBlind
 * @date 2022/8/14
 */

public sealed class LinuxBashClientHandler extends ClientHandler permits LinuxZshClientHandler, UbuntuBashClientHandler {

    /**
     * 软链接标识
     */
    final char link = 'l';

    /**
     * 文件标识
     */
    final char file = '-';
    final String CONTENT = "\\S+]\\d+;\\w+@\\w+\\S+";


    final Pattern p = Pattern.compile(CONTENT);

    public LinuxBashClientHandler(TtyType ttyType) {
        super(ttyType);
    }


    @Override
    public boolean isInCommandInput(String rowText) {
        System.out.println("rowText:  " + rowText);
        Matcher m = p.matcher(rowText);
        boolean b = m.find();
        System.out.println("find " + b);
        return b;
    }


    @Override
    public List<FileInfo> getFileInfos(Function<String, String> exec, String completePath) {
        // |grep ^d
        String separator = getSeparator();
        String selectPath = completePath.endsWith(separator) ? completePath : completePath + separator;
        String resultListStr = exec.apply("ls -Al --time-style '+%Y/%m/%d %H:%M' " + selectPath);
        List<String> resultList = resultListStr.lines().skip(1).toList();
        List<FileInfo> fileInfos = new ArrayList<>(resultList.size());
        resultList.stream().forEach((line) -> {
            String[] lineSplits = line.split("\\s+", 8);
            char[] typeAndPermission = lineSplits[0].toCharArray();
            char mode = typeAndPermission[0];
            if (mode == link) {
                return;
            }

            FileType fileType = switch (mode) {
                case '-' -> FILE;
                case 'd' -> FileType.DIRECTORY;
                default -> throw new IllegalArgumentException("");
            };
            final int fileNameIndex = 7;
            String fileName = lineSplits[fileNameIndex];
            String modifyDate = String.format("%s %s", lineSplits[5], lineSplits[6]);
            fileInfos.add(new FileInfo(selectPath + fileName, fileName, Long.parseLong(lineSplits[4]), fileType, modifyDate));

        });
        return fileInfos;
    }


    @Override
    public void createFile(Function<String, String> exec, String completePath, FileType fileType) {
        String shellTemplate =
                switch (fileType) {
                    case FILE -> "touch %s \n";
                    case DIRECTORY -> "mkdir %s \n";
                    case null -> throw new IllegalArgumentException("");
                };
        exec.apply(shellTemplate.formatted(completePath));
    }

    @Override
    public String getSeparator() {
        return StrUtil.SLASH;
    }
}
