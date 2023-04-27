package com.tshell.controller;

import com.tshell.common.response.BaseResponse;
import com.tshell.module.dto.session.AddSshSessionDTO;
import com.tshell.module.dto.session.UpdSshSessionDTO;
import com.tshell.module.entity.Session;
import com.tshell.module.entity.SshSession;
import com.tshell.service.SshSessionService;
import io.smallrye.common.annotation.RunOnVirtualThread;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;

/**
 * https://cn.quarkus.io/guides/hibernate-orm-panache
 * <p>
 * 主机管理
 *
 * @author TheBlind
 * @version 1.0
 */
@Path("/sshSession")
@Produces(MediaType.APPLICATION_JSON)
@RunOnVirtualThread
public class SshSessionController {

    private static final Logger log = LoggerFactory.getLogger(SshSessionController.class);

    @Inject
    SshSessionService sshSessionService;


    @GET
    @Path("{id}")
    public BaseResponse getSingle(@PathParam("id") String id) {
        return BaseResponse.ok(sshSessionService.getSingle(id));
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Transactional(rollbackOn = Exception.class)
    public BaseResponse<Boolean> create(@Valid AddSshSessionDTO addSshSessionDTO) {
        return BaseResponse.ok(sshSessionService.create(addSshSessionDTO));
    }

    @PUT
    @Path("{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Transactional(rollbackOn = Exception.class)
    public BaseResponse<Boolean> update(@PathParam("id") String id, @Valid UpdSshSessionDTO updSshSessionDTO) {
        return BaseResponse.ok(sshSessionService.update(updSshSessionDTO));
    }


    @PATCH
    @Path("/{id}/group")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Transactional(rollbackOn = Exception.class)
    public BaseResponse<Boolean> updGroup(@PathParam("id") Integer id, @NotNull @FormParam("groupId") Integer groupId) {
        return BaseResponse.ok(sshSessionService.updGroup(id, groupId));
    }


    @DELETE
    @Path("{id}")
    @Transactional(rollbackOn = Exception.class)
    public BaseResponse delete(@PathParam("id") String id) {

        Session entity = Session.findById(id);
        SshSession.delete("sessionId = ?1",id);
        if (entity == null) {
            throw new WebApplicationException("Fruit with sessionId of " + id + " does not exist.", 404);
        }
        entity.delete();
        return BaseResponse.ok();
    }


}
