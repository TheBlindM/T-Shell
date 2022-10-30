package com.tshell.controller;

import com.tshell.common.response.BaseResponse;
import com.tshell.module.dto.PageDTO;
import com.tshell.module.dto.historyCmd.AddHistoryCmdDTO;
import com.tshell.module.dto.historyCmd.GetHistoryCmdPageDTO;
import com.tshell.module.entity.HistoryCmd;
import com.tshell.module.vo.PageVO;
import com.tshell.service.HistoryCmdService;
import io.smallrye.common.annotation.RunOnVirtualThread;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

/**
 * 历史命令
 * @author TheBlind
 * @version 1.0
 */
@Path("/historyCmd")
@Produces(MediaType.APPLICATION_JSON)
@RunOnVirtualThread
public class HistoryCmdController {

    private static final Logger log = LoggerFactory.getLogger(HistoryCmdController.class);

    @Inject
    HistoryCmdService historyCmdService;


    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Transactional(rollbackOn = Exception.class)
    public BaseResponse create(AddHistoryCmdDTO addHistoryCmdDTO) {
        historyCmdService.create(addHistoryCmdDTO);
        return BaseResponse.ok();
    }


    @POST
    @Path("/getPage")
    @Consumes(MediaType.APPLICATION_JSON)
    public BaseResponse<PageVO<HistoryCmd>> getPage(PageDTO<GetHistoryCmdPageDTO> pageDTO) {
        return BaseResponse.ok(historyCmdService.getPage(pageDTO));
    }


}
