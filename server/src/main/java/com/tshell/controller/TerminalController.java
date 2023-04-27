package com.tshell.controller;

import com.tshell.common.response.BaseResponse;
import com.tshell.core.FileInfo;
import com.tshell.core.TtySize;
import com.tshell.core.TerminalService;
import com.tshell.module.dto.terminal.LocalInitConnectDTO;
import com.tshell.module.dto.terminal.SshInitConnectDTO;
import com.jcraft.jsch.JSchException;
import io.smallrye.common.annotation.RunOnVirtualThread;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import java.io.IOException;
import java.util.List;

/**
 * 终端Controller
 *
 * @author TheBlind
 * @date 2022/7/9
 */
@Path("/terminal")
@Produces(MediaType.APPLICATION_JSON)
@RunOnVirtualThread
public class TerminalController {

    @Inject
    TerminalService terminalService;


    @Path("/sshInitConnect")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public BaseResponse initConnect(SshInitConnectDTO sshInitConnectDTO) throws IOException, JSchException {
        terminalService.initSshConnection(sshInitConnectDTO);
        return BaseResponse.ok();
    }


    @Path("/localInitConnect")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public BaseResponse localInitConnect(LocalInitConnectDTO localInitConnectDTO) throws IOException, JSchException {
        terminalService.initLocalConnection(localInitConnectDTO);
        return BaseResponse.ok(true);
    }


    @Path("/close/{channelId}")
    @DELETE
    @Consumes(MediaType.APPLICATION_JSON)
    public BaseResponse close(@PathParam("channelId") String channelId) throws IOException {
        terminalService.close(channelId);
        return BaseResponse.ok();
    }


    @Path("/resize/{channelId}")
    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    public BaseResponse resize(@PathParam("channelId") String channelId, TtySize ttySize) throws IOException {
        terminalService.changeWindow(channelId, ttySize);
        return BaseResponse.ok();
    }

    @Path("/isInCommandInput/{channelId}")
    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    public BaseResponse isInCommandInput(@PathParam("channelId") String channelId) {
        terminalService.isInCommandInput(channelId);
        return BaseResponse.ok();
    }


    @Path("/sessionCount/{channelId}")
    @GET
    public BaseResponse<Integer> getSessionCount(@PathParam("channelId") String channelId) {
        return BaseResponse.ok(terminalService.getSessionCount(channelId));
    }


}
