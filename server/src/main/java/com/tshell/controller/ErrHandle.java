package com.tshell.controller;

import cn.hutool.json.JSONUtil;
import com.tshell.common.response.BaseResponse;
import lombok.extern.slf4j.Slf4j;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

/**
 * @author TheBlind
 * @date 2022/5/8
 */
@Slf4j
@Provider
public class ErrHandle implements ExceptionMapper<Exception> {

    @Override
    public Response toResponse(Exception exception){
        log.error("失败 to handle request", exception);
        int code = 500;
        if (exception instanceof WebApplicationException) {
            code = ((WebApplicationException) exception).getResponse().getStatus();
        }
        BaseResponse response = new BaseResponse();
        response.setCode(code);
        response.setMessage(exception.getMessage());

        if (log.isDebugEnabled()) {
            response.setDevMessage(exception.getMessage());
        }
        return Response.ok()
                .entity(JSONUtil.toJsonStr(response))
                .build();
    }
}
