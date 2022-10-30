package com.tshell.module.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.quarkus.runtime.annotations.RegisterForReflection;

import java.util.List;

/**
 * @author TheBlind
 * @date 2022/7/17
 */
@RegisterForReflection
public record RetrieveMenuVO(
        @JsonProperty("isInCommandInput")
        boolean isInCommandInput,
        @JsonProperty("retrieves")
        List<RetrieveVO> retrieves
) {
}
