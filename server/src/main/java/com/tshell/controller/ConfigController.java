package com.tshell.controller;

import com.tshell.common.response.BaseResponse;
import com.tshell.service.ConfigService;
import io.smallrye.common.annotation.RunOnVirtualThread;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

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
