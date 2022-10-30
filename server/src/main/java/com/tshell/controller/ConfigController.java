package com.tshell.controller;

import com.tshell.common.response.BaseResponse;
import com.tshell.service.ConfigService;
import io.smallrye.common.annotation.RunOnVirtualThread;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

/**
 * 配置
 *
 * @author TheBlind
 * @version 1.0
 */
@Path("/config")
@Produces(MediaType.APPLICATION_JSON)
@RunOnVirtualThread
public class ConfigController {

    @Inject
    ConfigService configService;


    @GET
    @Path("/exportData")
    public BaseResponse<Boolean> exportData() {
        configService.exportData();
        return BaseResponse.ok(true);
    }


}
