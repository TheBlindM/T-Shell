package com.tshell.controller;

import com.tshell.common.response.BaseResponse;
import com.tshell.module.dto.retrieve.ParseTemplateDTO;
import com.tshell.service.RetrieveService;
import io.smallrye.common.annotation.RunOnVirtualThread;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import java.util.Map;

/**
 * 检索控制台
 * 命令提示
 *
 * @author TheBlind
 * @date 2022/7/14
 */
@Path("/retrieve")
@Produces(MediaType.APPLICATION_JSON)
@RunOnVirtualThread
public class RetrieveController {

    @Inject
    RetrieveService retrieveService;



    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("parseTemplate")
    public BaseResponse<Boolean> parseTemplate(ParseTemplateDTO parseTemplateDTO) throws Exception {
        retrieveService.parseTemplate(parseTemplateDTO);
        return BaseResponse.ok(true);
    }

    @GET()
    @Path("matchItems")
    public BaseResponse<Map<String, String>> getMatchItems(@QueryParam("id") Integer id, @QueryParam("channelId") String channelId) throws Exception {
        return BaseResponse.ok(retrieveService.getMatchItems(id, channelId));
    }


}
