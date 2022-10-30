package com.tshell.core.client;

import cn.hutool.core.util.StrUtil;
import com.tshell.core.FileInfo;
import com.tshell.core.FileType;

import java.util.List;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author TheBlind
 * @date 2022/8/14
 */

public class CmdClientHandler extends ClientHandler {

    private final String CONTENT="[\\S]+@[\\S]+ [\\S]+>";
    private final String CONTENT1="[\\S]+@[\\S]+ [\\S]+>[\\W\\S]+conhost.exe$";
    private final String CONTENT2="[\\S]+@[\\S]+ [\\S]+>[\\W\\S]+conhost.exe$";
    private final Pattern p = Pattern.compile(CONTENT);

    public CmdClientHandler(TtyType ttyType) {
        super(ttyType);
    }

    @Override
    public boolean isInCommandInput(String rowText) {

        String[] split = rowText.split(StrUtil.LF);

        String originalPrefix = split[split.length-1].trim();
        String exg=CONTENT;
        if(originalPrefix.endsWith("conhost.exe")){
            exg=CONTENT1;
        }

        Matcher m = p.matcher(originalPrefix);
        return  m.find();
    }

    @Override
    public List<FileInfo> getFileInfos(Function<String, String> exec, String completePath) {
        return null;
    }

    @Override
    public String getSeparator() {
        return StrUtil.SLASH;
    }

    @Override
    public void createFile(Function<String, String> exec, String completePath, FileType fileType) {

    }
}
