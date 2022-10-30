package com.tshell.controller;

import com.tshell.common.response.BaseResponse;
import com.tshell.module.vo.connectionLog.TopVO;
import com.tshell.service.ConnectionLogService;
import io.smallrye.common.annotation.RunOnVirtualThread;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.List;

/**
 * 连接日志
 *
 * @author TheBlind
 * @version 1.0
 */
@Path("/connectionLog")
@Produces(MediaType.APPLICATION_JSON)
@RunOnVirtualThread
public class ConnectionLogController {

    @Inject
    ConnectionLogService connectionLogService;


    @GET
    @Path("/topList")
    public BaseResponse<List<TopVO>> topList() {
        return BaseResponse.ok(connectionLogService.topList());
    }


}
