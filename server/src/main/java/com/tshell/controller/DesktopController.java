package com.tshell.controller;

import com.tshell.common.response.BaseResponse;
import com.tshell.service.DesktopService;
import io.smallrye.common.annotation.RunOnVirtualThread;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.io.IOException;

/**
 * @author TheBlind
 * @version 1.0
 */
@Path("/desktop")
@Produces(MediaType.APPLICATION_JSON)
@RunOnVirtualThread
public class DesktopController {

    @Inject
    DesktopService desktopService;

    @POST
    @Path("/toGithub")
    @Consumes(MediaType.APPLICATION_JSON)
    public BaseResponse<Boolean> toGithub() throws IOException {
        desktopService.toGithub();
        return BaseResponse.ok(true);
    }



}
