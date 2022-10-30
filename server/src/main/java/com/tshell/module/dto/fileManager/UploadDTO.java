package com.tshell.module.dto.fileManager;

import lombok.Data;
import org.jboss.resteasy.annotations.providers.multipart.PartType;

import javax.ws.rs.FormParam;
import javax.ws.rs.core.MediaType;
import java.io.InputStream;
import java.util.List;

public record UploadDTO(

        String path,

         List<String> filePaths
){

}

/*
@Data
public class UploadDTO {
    @FormParam("path")
    String path;
    @FormParam("filename")
    String filename;

    @PartType(MediaType.APPLICATION_OCTET_STREAM)
    @FormParam("file")
    InputStream file;
}
*/
