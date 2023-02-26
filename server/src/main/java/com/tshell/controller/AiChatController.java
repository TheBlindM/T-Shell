package com.tshell.controller;

import com.tshell.common.response.BaseResponse;
import com.tshell.service.AiServer;
import io.smallrye.common.annotation.RunOnVirtualThread;

import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

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
