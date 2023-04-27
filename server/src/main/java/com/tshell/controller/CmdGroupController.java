package com.tshell.controller;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.lang.tree.Tree;
import cn.hutool.core.lang.tree.TreeNodeConfig;
import cn.hutool.core.lang.tree.TreeUtil;
import com.tshell.module.entity.Session;
import com.tshell.module.entity.SshSession;
import com.tshell.module.entity.SessionGroup;
import io.smallrye.common.annotation.RunOnVirtualThread;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author TheBlind
 * @date 2022/7/4
 */
@Path("/cmdGroup")
@Produces(MediaType.APPLICATION_JSON)
@RunOnVirtualThread
public class CmdGroupController {

    private static final Logger log = LoggerFactory.getLogger(CmdGroupController.class);

    @GET
    public Response get() {
        // Sort.by("id")
        return Response.ok(SessionGroup.listAll()).build();
    }

    @GET
    @Path("/tree")
    public Response getTree() {
        List<SessionGroup> list = SessionGroup.listAll();
        List<Tree<String>> build = TreeUtil.build(list, "0", TreeNodeConfig.DEFAULT_CONFIG, (object, tree) -> {
            tree.setId(String.valueOf(object.id));
            tree.setParentId(String.valueOf(object.getParentId()));
            tree.setName(object.getGroupName());
            List<Session> sshSessions = Session.list("sessionGroupId", object.id);
            List<Tree<String>> children = sshSessions.stream().map((session) -> {
                Tree<String> objectTree = new Tree<>();
                objectTree.setParentId(String.valueOf(object.id));
                objectTree.setName(session.getSessionName());
                objectTree.setId(session.id);
                return objectTree;
            }).collect(Collectors.toList());
            tree.setChildren(children);
        });
        return Response.ok(build).build();
    }

    @GET
    @Path("{id}")
    public Response getSingle(@PathParam("id") Integer id) {
        return SshSession.findByIdOptional(id).map(host -> Response.ok(host).build())
                .orElse(Response.status(Response.Status.NOT_FOUND).build());
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Transactional(rollbackOn = Exception.class)
    public Response create(SessionGroup sessionGroup) {
        sessionGroup.persist();
        return Response.ok(sessionGroup).build();
    }

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("{id}")
    public Response update(@PathParam("id") Integer id, SshSession sshSession) {
        SshSession entity = SshSession.findById(id);
        if (entity == null) {
            throw new WebApplicationException("Fruit with id of " + id + " does not exist.", 404);
        }
        BeanUtil.copyProperties(sshSession, entity);
        return Response.ok(sshSession).build();
    }

    @DELETE
    @Path("{id}")
    public Response delete(@PathParam("id") Integer id) {
        SessionGroup entity = SessionGroup.findById(id);
        if (entity == null) {
            throw new WebApplicationException("Fruit with id of " + id + " does not exist.", 404);
        }
        entity.delete();
        return Response.ok().build();
    }


}
