package com.tshell.common.response;

import io.quarkus.runtime.annotations.RegisterForReflection;

/**
 * @author TheBlind
 * @date 2022/8/2
 */
@RegisterForReflection
public record PageData (

        int page,
        Object data,
        int pageCount,
        int pageSize
){
}
