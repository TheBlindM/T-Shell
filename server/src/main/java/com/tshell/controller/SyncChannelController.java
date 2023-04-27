package com.tshell.controller;

import com.tshell.common.response.BaseResponse;
import com.tshell.service.SyncChannelService;
import io.smallrye.common.annotation.RunOnVirtualThread;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;

/**
 *
 * 频道同步
 * @author TheBlind
 */
@Path("/syncChannel")
@Produces(MediaType.APPLICATION_JSON)
@RunOnVirtualThread
public class SyncChannelController {

    @Inject
    SyncChannelService syncChannelService;

    @GET
    public BaseResponse<String> get(@QueryParam("sshChannelId")String sshChannelId){
        return BaseResponse.ok("success", syncChannelService.get(sshChannelId));
    }

    @POST
    @Path("/bind")
    public BaseResponse bind(@FormParam("syncChannelId")String syncChannelId,@FormParam("sshChannelId") String sshChannelId) {
        syncChannelService.bind(syncChannelId,sshChannelId);
        return BaseResponse.ok();
    }

    @POST
    @Path("/removeBind")
    public BaseResponse remove(@FormParam("sshChannelId") String sshChannelId) {
        syncChannelService.remove(sshChannelId);
        return BaseResponse.ok();
    }



}
