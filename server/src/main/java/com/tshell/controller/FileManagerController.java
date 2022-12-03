package com.tshell.controller;

import com.tshell.common.response.BaseResponse;
import com.tshell.core.FileInfo;
import com.tshell.core.FileManagerService;
import com.tshell.module.dto.fileManager.CreateDTO;
import com.tshell.module.dto.fileManager.UploadDTO;
import com.tshell.module.vo.CompleteTransferRecordVO;
import io.smallrye.common.annotation.RunOnVirtualThread;

import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.List;

/**
 * 文件管理器
 *
 * @author TheBlind
 * @version 1.0
 */
@Path("/fileManager")
@Produces(MediaType.APPLICATION_JSON)
@RunOnVirtualThread
public class FileManagerController {

    @Inject
    FileManagerService fileManagerService;


    @Path("/create/{channelId}")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public BaseResponse create(@PathParam("channelId") String channelId, CreateDTO createDTO) {
        fileManagerService.create(channelId, createDTO);
        return BaseResponse.ok();
    }


    @Path("/upload/{channelId}")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public BaseResponse upload(@PathParam("channelId") String channelId, UploadDTO uploadDTO) {
        fileManagerService.upload(channelId, uploadDTO);
        return BaseResponse.ok(true);
    }


    @Path("/removeFile/{channelId}")
    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public BaseResponse removeFile(@PathParam("channelId") String channelId, @FormParam("path") String path) {
        System.out.println(path);
        fileManagerService.removeFile(channelId, path);
        return BaseResponse.ok();
    }

    @Path("/removeDirectory/{channelId}")
    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public BaseResponse removeDirectory(@PathParam("channelId") String channelId, @FormParam("path") String path) {
        fileManagerService.removeDirectory(channelId, path);
        return BaseResponse.ok();
    }


    @Path("/rename/{channelId}")
    @PUT
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public BaseResponse rename(@PathParam("channelId") String channelId, @FormParam("oldPath") String oldPath, @FormParam("fileName") String fileName) {
        fileManagerService.rename(channelId, oldPath, fileName);
        return BaseResponse.ok();
    }

    @Path("/download/{channelId}")
    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public BaseResponse download(@PathParam("channelId") String channelId, @FormParam("path") String path) {
        fileManagerService.download(channelId, path);
        return BaseResponse.ok();
    }


    @Path("/fileInfos/{channelId}")
    @GET
    public BaseResponse<List<FileInfo>> getFileInfos(@PathParam("channelId") String channelId, @QueryParam("path") String path) {
        return BaseResponse.ok(fileManagerService.fileInfos(channelId, path));
    }


    @Path("/pauseTransfer/{channelId}/{transferRecordId}")
    @POST
    @Transactional(rollbackOn = Exception.class)
    public BaseResponse pauseTransfer(@PathParam("channelId") String channelId, @PathParam("transferRecordId") String transferRecordId) {
        fileManagerService.pauseTransfer(channelId, transferRecordId);
        return BaseResponse.ok();
    }


    @Path("/continueTransfer/{channelId}/{transferRecordId}")
    @POST
    @Transactional(rollbackOn = Exception.class)
    public BaseResponse continueTransfer(@PathParam("channelId") String channelId, @PathParam("transferRecordId") String transferRecordId) {
        fileManagerService.continueTransfer(channelId, transferRecordId);
        return BaseResponse.ok();
    }


    @Path("/completeList/{channelId}")
    @GET
    public BaseResponse<List<CompleteTransferRecordVO>> getCompleteList(@PathParam("channelId") String channelId) {
        return BaseResponse.ok(fileManagerService.getCompleteList(channelId));
    }

    @Path("/downloadList/{channelId}")
    @GET
    public BaseResponse<List<FileManagerService.Progress>> getDownloadList(@PathParam("channelId") String channelId) {
        return BaseResponse.ok(fileManagerService.getDownloadList(channelId));
    }

    @Path("/uploadList/{channelId}")
    @GET
    public BaseResponse<List<FileManagerService.Progress>> getUploadList(@PathParam("channelId") String channelId) {
        return BaseResponse.ok(fileManagerService.getUploadList(channelId));
    }

    @Path("/transferCount/{channelId}")
    @GET
    public BaseResponse<List<FileManagerService.Progress>> getTransferCount(@PathParam("channelId") String channelId) {
        return BaseResponse.ok(fileManagerService.getUploadList(channelId));
    }


    @DELETE
    @Path("/record/{transferRecordId}")
    @Transactional(rollbackOn = Exception.class)
    public BaseResponse<Boolean> deleteRecord(@PathParam("transferRecordId") String transferRecordId) {
        fileManagerService.deleteRecord(transferRecordId);
        return BaseResponse.ok(true);
    }

    @GET
    @Path("/openFile/{channelId}")
    @Transactional(rollbackOn = Exception.class)
    public BaseResponse<Boolean> openFile(@PathParam("channelId") String channelId, @QueryParam("path") String path) {
        fileManagerService.openFile(channelId, path);
        return BaseResponse.ok(true);
    }

    @DELETE
    @Path("/openFile")
    @Transactional(rollbackOn = Exception.class)
    public BaseResponse<Boolean> cancelOpenFile(@QueryParam("taskId") String taskId) {
        fileManagerService.cancelOpenFile(taskId);
        return BaseResponse.ok(true);
    }


}
