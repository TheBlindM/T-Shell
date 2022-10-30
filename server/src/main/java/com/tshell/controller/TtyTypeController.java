package com.tshell.controller;

import cn.hutool.core.lang.tree.Tree;
import cn.hutool.core.lang.tree.TreeNodeConfig;
import cn.hutool.core.lang.tree.TreeUtil;
import com.tshell.common.response.BaseResponse;
import com.tshell.core.client.TtyType;
import com.tshell.module.entity.OsType;
import io.smallrye.common.annotation.RunOnVirtualThread;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.transaction.Transactional;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.Arrays;
import java.util.List;

/**
 *  Windows系统终端为cmd;
 *  Linux操作系统为bash;
 *  OS X系统终端为Terminal
 * 操作系统
 * @author TheBlind
 * @version 1.0
 */
@Path("/ttyType")
@Produces(MediaType.APPLICATION_JSON)
@RunOnVirtualThread
public class TtyTypeController {

    private static final Logger log = LoggerFactory.getLogger(TtyTypeController.class);

    @GET
    @Path("listById")
    @Transactional(rollbackOn = Exception.class)
    public BaseResponse getListById(@QueryParam("ttyTypeIds[]") List<Integer> ttyTypeIds) {
        return BaseResponse.ok(Arrays.stream(TtyType.values()).filter(ptyOsType ->
                ttyTypeIds.contains(ptyOsType.getId())
        ).map((ptyOsType -> OsType.builder().id(ptyOsType.getId()).osTypeName(ptyOsType.getName()).build())).toList());
    }





    @GET
    @Path("/selectTree")
    public BaseResponse getSelectTree() {
        TreeNodeConfig treeNodeConfig = new TreeNodeConfig();
        List<TtyType> ttyTypes = Arrays.asList(TtyType.values());

        List<Tree<Integer>> build = TreeUtil.<TtyType, Integer>build(ttyTypes, 0, treeNodeConfig, (object, tree) -> {
            tree.setId(object.getId());
            tree.setParentId(object.getParentId());
            tree.setName(object.getName());
        });
        return BaseResponse.ok(build);
    }



}
