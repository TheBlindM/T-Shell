package com.tshell.controller;

import com.tshell.common.response.BaseResponse;
import com.tshell.module.dto.config.AppearanceDTO;
import com.tshell.module.dto.config.TerminalDTO;
import com.tshell.module.entity.Config;
import com.tshell.module.vo.config.AppearanceVO;
import com.tshell.module.vo.config.TerminalVO;
import com.tshell.service.ConfigService;
import io.smallrye.common.annotation.RunOnVirtualThread;

import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
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


    @GET
    @Path("/appearance")
    public BaseResponse<AppearanceVO> getAppearance() {
        return BaseResponse.ok(configService.getAppearance());
    }
    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/appearance")
    public BaseResponse<AppearanceVO> updateAppearance(@Valid AppearanceDTO appearanceDTO) throws Exception {
        return BaseResponse.ok(configService.updateAppearance(appearanceDTO));
    }


    @GET
    @Path("/terminal")
    public BaseResponse<TerminalVO> getTerminal() {
        return BaseResponse.ok(configService.getTerminal());
    }
    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/terminal")
    public BaseResponse<TerminalVO> updateTerminal(@Valid TerminalDTO terminalDTO) throws Exception {
        return BaseResponse.ok(configService.updateTerminal(terminalDTO));
    }


    @GET
    @Path("")
    public BaseResponse<Config> getConfig() {
        return BaseResponse.ok(configService.getSingle());
    }

}
