package com.tshell.controller;

import com.tshell.common.response.BaseResponse;
import com.tshell.service.AiServer;
import io.smallrye.common.annotation.RunOnVirtualThread;

import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;

/**
 * ai聊天
 * @author TheBlind
 * @version 1.0
 */
@Path("/ai")
@Produces(MediaType.APPLICATION_JSON)
@RunOnVirtualThread
public class AiChatController {
    @Inject
    AiServer aiServer;

    @GET
    @Path("/chat")
    @Transactional(rollbackOn = Exception.class)
    public BaseResponse getListById(@QueryParam("question") String question) {
        return BaseResponse.ok("success",aiServer.doChat(question));
    }


}
