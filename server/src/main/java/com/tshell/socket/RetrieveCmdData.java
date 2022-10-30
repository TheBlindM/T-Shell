package com.tshell.socket;


import com.fasterxml.jackson.annotation.JsonProperty;
import io.quarkus.runtime.annotations.RegisterForReflection;

/**
 * @author TheBlind
 * @date 2022/8/23
 */
@RegisterForReflection
public record RetrieveCmdData(
        @JsonProperty("term")
        String term,
        @JsonProperty("skipVerify")
        boolean skipVerify) {
}
