package com.tshell.controller;

import cn.hutool.core.lang.tree.Tree;
import cn.hutool.core.lang.tree.TreeNodeConfig;
import cn.hutool.core.lang.tree.TreeUtil;
import com.tshell.common.response.BaseResponse;
import com.tshell.module.dto.cmd.AddCmdDto;
import com.tshell.module.dto.cmd.UpdCmdDto;
import com.tshell.module.entity.Cmd;
import com.tshell.module.vo.CmdVO;
import com.tshell.service.CmdService;
import io.smallrye.common.annotation.RunOnVirtualThread;

import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import java.util.List;

/**
 * @author TheBlind
 * @version 1.0
 */
@Path("/cmd")
@Produces(MediaType.APPLICATION_JSON)
@RunOnVirtualThread
public class CmdController {

    @Inject
    CmdService cmdService;


    @GET
    public BaseResponse get() {
        return BaseResponse.ok(Cmd.listAll());
    }

    @GET
    @Path("{id}")
    public BaseResponse<CmdVO> getSingle(@PathParam("id") Integer id) {
        return BaseResponse.ok(cmdService.getSingle(id));
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Transactional(rollbackOn = Exception.class)
    public BaseResponse create(AddCmdDto cmdDto) {
        return BaseResponse.ok(cmdService.create(cmdDto));
    }

    @PUT
    @Path("{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Transactional(rollbackOn = Exception.class)
    public BaseResponse update(@PathParam("id") Integer id, UpdCmdDto updCmdDto) {
        return BaseResponse.ok(cmdService.update(updCmdDto));
    }

    @DELETE
    @Path("{id}")
    @Transactional(rollbackOn = Exception.class)
    public BaseResponse delete(@PathParam("id") Integer id) {
        return BaseResponse.ok(cmdService.delete(id));
    }


    /**
     * @return
     */
    @GET
    @Path("/tree")
    public BaseResponse getTree() {
        List<Cmd> list = Cmd.listAll();
        List<Tree<Integer>> build = TreeUtil.<Cmd, Integer>build(list, 0, TreeNodeConfig.DEFAULT_CONFIG, (object, tree) -> {
            tree.setId(object.id);
            tree.setParentId(object.getParentCmdId());
            tree.setName(object.getCmdText());
        });
        return BaseResponse.ok(build);
    }


}
