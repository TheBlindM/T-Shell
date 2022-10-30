package com.tshell.controller;

import cn.hutool.core.lang.tree.Tree;
import cn.hutool.core.lang.tree.TreeNodeConfig;
import cn.hutool.core.lang.tree.TreeUtil;
import com.tshell.common.response.BaseResponse;
import com.tshell.module.dto.hostGroup.AddHostGroupDTO;
import com.tshell.module.dto.hostGroup.UpdHostGroupDTO;
import com.tshell.module.entity.Session;
import com.tshell.module.entity.SessionGroup;
import com.tshell.service.HostGroupService;
import io.smallrye.common.annotation.RunOnVirtualThread;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.validation.constraints.NotNull;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;



/**
 * 会话组
 *
 * @author TheBlind
 * @version 1.0
 */
@Path("/sessionGroup")
@Produces(MediaType.APPLICATION_JSON)
@RunOnVirtualThread
public class SessionGroupController {

    @Inject
    HostGroupService hostGroupService;

    @GET
    public BaseResponse<List<SessionGroup>> get() {
        return BaseResponse.ok(SessionGroup.listAll());
    }

    @GET
    @Path("/tree")
    @Transactional(rollbackOn = Exception.class)
    public BaseResponse<List<Tree<String>>> getTree() {
        List<SessionGroup> list = SessionGroup.listAll();
        if (list.isEmpty()) {
            return BaseResponse.ok(Collections.emptyList());
        }
        List<Tree<String>> build = TreeUtil.build(list, "0", TreeNodeConfig.DEFAULT_CONFIG, (object, tree) -> {
            tree.setId(String.valueOf(object.id));
            tree.setParentId(String.valueOf(object.getParentId()));
            tree.setName(object.getGroupName());
            List<Session> hosts = Session.list("sessionGroupId", object.id);
            tree.put("type","group");
            List<Tree<String>> children = hosts.stream().map((host) -> {
                Tree<String> objectTree = new Tree<>();
                objectTree.setParentId(String.valueOf(object.id));
                objectTree.setName(host.getSessionName());
                objectTree.setId(host.id);
                objectTree.put("type","session");
                return objectTree;
            }).collect(Collectors.toList());
            tree.setChildren(children);
        });
        return BaseResponse.ok(build);
    }

    @GET
    @Path("{id}")
    public BaseResponse getSingle(@PathParam("id") Integer id) {
        return SessionGroup.<SessionGroup>findByIdOptional(id).map(BaseResponse::ok)
                .orElse(BaseResponse.err(Response.Status.NOT_FOUND, "Fruit with id of " + id + " does not exist."));
    }


    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Transactional(rollbackOn = Exception.class)
    public BaseResponse create(AddHostGroupDTO addHostGroupDTO) {
        return BaseResponse.ok(hostGroupService.create(addHostGroupDTO));
    }

    @PUT
    @Path("{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Transactional(rollbackOn = Exception.class)
    public BaseResponse update(@PathParam("id") Integer id, UpdHostGroupDTO updHostGroupDTO) {
        return BaseResponse.ok(hostGroupService.update(updHostGroupDTO));
    }


    @PATCH
    @Path("/{id}/group")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Transactional(rollbackOn = Exception.class)
    public BaseResponse updGroup(@PathParam("id") Integer id, @NotNull @FormParam("groupId") Integer groupId) {
        SessionGroup entity = SessionGroup.findById(id);
        if (entity == null) {
            throw new WebApplicationException("Fruit with id of " + id + " does not exist.", 404);
        }
        entity.setParentId(groupId);
        return BaseResponse.ok(entity);
    }

    @DELETE
    @Path("{id}")
    @Transactional(rollbackOn = Exception.class)
    public BaseResponse delete(@PathParam("id") Integer id) {
        SessionGroup entity = SessionGroup.findById(id);
        if (entity == null) {
            throw new WebApplicationException("Fruit with id of " + id + " does not exist.", 404);
        }
        entity.delete();
        return BaseResponse.ok();
    }


}
