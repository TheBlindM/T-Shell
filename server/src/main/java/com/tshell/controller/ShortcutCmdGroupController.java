package com.tshell.controller;

import cn.hutool.core.lang.tree.Tree;
import cn.hutool.core.lang.tree.TreeNodeConfig;
import cn.hutool.core.lang.tree.TreeUtil;
import com.tshell.common.response.BaseResponse;
import com.tshell.module.entity.GroupShortcutCmd;
import com.tshell.module.entity.ShortcutCmd;
import com.tshell.module.entity.ShortcutCmdGroup;
import io.smallrye.common.annotation.RunOnVirtualThread;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.transaction.Transactional;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author TheBlind
 * @version 1.0
 */
@Path("/shortcutCmdGroup")
@Produces(MediaType.APPLICATION_JSON)
@RunOnVirtualThread
public class ShortcutCmdGroupController {

    private static final Logger log = LoggerFactory.getLogger(ShortcutCmdGroupController.class);

    @GET
    public BaseResponse get() {
        // Sort.by("id")
        return BaseResponse.ok(ShortcutCmdGroup.listAll());
    }

    @GET
    @Path("/tree")
    public BaseResponse getTree() {
        List<ShortcutCmdGroup> list = ShortcutCmdGroup.listAll();

        if (list.isEmpty()) {
            return BaseResponse.ok(list);
        }

        List<Tree<Integer>> build = TreeUtil.<ShortcutCmdGroup, Integer>build(list, 0,  TreeNodeConfig.DEFAULT_CONFIG, (object, tree) -> {
            tree.setId(object.id);
            tree.setParentId(object.getParentId());
            tree.setName(object.getGroupName());
            List<GroupShortcutCmd> groupShortcutCmdList = GroupShortcutCmd.list("shortcutCmdGroupId", object.id);

            List<Integer> collect = groupShortcutCmdList.stream().map(GroupShortcutCmd::getShortcutCmdId).collect(Collectors.toUnmodifiableList());
            List<Tree<Integer>> children = ShortcutCmd.<ShortcutCmd>list("id  in  ?1", collect).stream().map(shortcutCmd -> {
                Tree<Integer> objectTree = new Tree<>();
                objectTree.setParentId(shortcutCmd.id);
                objectTree.setName(shortcutCmd.getName());
                objectTree.setId(-shortcutCmd.id);
                return objectTree;

            }).collect(Collectors.toList());
            tree.setChildren(children);
        });
        return BaseResponse.ok(build);
    }


    @GET
    @Path("/parentTree")
    public BaseResponse getParentTree() {
        List<ShortcutCmdGroup> list = ShortcutCmdGroup.listAll();

        if (list.isEmpty()) {
            return BaseResponse.ok(list);
        }
        List<Tree<Integer>> build = TreeUtil.<ShortcutCmdGroup, Integer>build(list, 0, TreeNodeConfig.DEFAULT_CONFIG, (object, tree) -> {
            tree.setId(object.id);
            tree.setParentId(object.getParentId());
            tree.setName(object.getGroupName());
        });
        return BaseResponse.ok(build);
    }

    @GET
    @Path("{id}")
    public BaseResponse getSingle(@PathParam("id") Integer id) {
        return ShortcutCmdGroup.<ShortcutCmdGroup>findByIdOptional(id).map(shortcutCmdGroup -> BaseResponse.ok(shortcutCmdGroup))
                .orElse(BaseResponse.err(Response.Status.NOT_FOUND, "Fruit with id of " + id + " does not exist."));
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Transactional(rollbackOn = Exception.class)
    public BaseResponse create(ShortcutCmdGroup shortcutCmdGroup) {
        shortcutCmdGroup.persist();
        return BaseResponse.ok(shortcutCmdGroup);
    }

    @PUT
    @Path("{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Transactional(rollbackOn = Exception.class)
    public BaseResponse update(@PathParam("id") Integer id, ShortcutCmdGroup shortcutCmdGroup) {
        ShortcutCmdGroup entity = ShortcutCmdGroup.findById(id);
        if (entity == null) {
            throw new WebApplicationException("Fruit with id of " + id + " does not exist.", 404);
        }
        entity.copyProperty(shortcutCmdGroup);
        return BaseResponse.ok(entity);
    }


    @DELETE
    @Path("{id}")
    @Transactional(rollbackOn = Exception.class)
    public BaseResponse delete(@PathParam("id") Integer id) {
        ShortcutCmdGroup entity = ShortcutCmdGroup.findById(id);
        if (entity == null) {
            throw new WebApplicationException("Fruit with id of " + id + " does not exist.", 404);
        }
        entity.delete();
        return BaseResponse.ok(true);
    }


}
