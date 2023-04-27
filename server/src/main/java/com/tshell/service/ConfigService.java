package com.tshell.service;

import cn.hutool.core.date.DateUtil;

import jakarta.enterprise.context.ApplicationScoped;
import java.io.File;
import java.nio.file.Path;

/**
 * @author TheBlind
 * @version 1.0
 */
@ApplicationScoped
public class ConfigService {
    final  String saveRootDir=System.getProperty("user.home");
    final String fileNamePrefix="T-Shell";
    final String fileNameSuffix=".data";
    record ExportData(String version,String jsonData){}

    public void exportData(){
        String time= DateUtil.now();
        String fileName="%s-%s%s".formatted(fileNameSuffix,time,fileNameSuffix);
        File file = Path.of(saveRootDir, fileName).toFile();


    }





}
