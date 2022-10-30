package com.tshell.controller;

import com.tshell.common.response.BaseResponse;
import com.tshell.module.dto.shortcutCmd.AddShortcutCmdDTO;
import com.tshell.module.dto.shortcutCmd.UpdShortcutCmdDTO;
import com.tshell.module.entity.Cmd;
import com.tshell.module.vo.ShortcutCmdVO;
import com.tshell.service.ShortcutCmdService;
import io.smallrye.common.annotation.RunOnVirtualThread;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.transaction.*;
import javax.transaction.NotSupportedException;
import javax.validation.Valid;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

/**
 * 用于实现快捷命令
 * <p>
 * -常用
 * -ip配置
 * ipconfig
 * ifconfig
 *
 * @author TheBlind
 * @version 1.0
 */
@Path("/shortcutCmd")
@Produces(MediaType.APPLICATION_JSON)
@RunOnVirtualThread
public class ShortcutCmdController {

    @Inject
    ShortcutCmdService shortcutCmdService;

    @GET
    public BaseResponse get() {
        // Sort.by("id")
        return BaseResponse.ok(Cmd.listAll());
    }

    @GET
    @Path("{id}")
    public BaseResponse<ShortcutCmdVO> getSingle(@PathParam("id") Integer id) {
        return BaseResponse.ok(shortcutCmdService.getSingle(id));
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public BaseResponse<Boolean> create(@Valid AddShortcutCmdDTO addShortcutCmdDTO) throws Exception {
        return BaseResponse.ok(shortcutCmdService.create(addShortcutCmdDTO));
    }

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("{id}")
    public BaseResponse update(@PathParam("id") Integer id, @Valid UpdShortcutCmdDTO updShortcutCmdDTO) throws Exception {
        return BaseResponse.ok(shortcutCmdService.update(updShortcutCmdDTO));
    }

    @DELETE
    @Path("{id}")
    @Transactional(rollbackOn = Exception.class)
    public BaseResponse delete(@PathParam("id") Integer id) {
        return BaseResponse.ok(shortcutCmdService.delete(id));
    }


}
