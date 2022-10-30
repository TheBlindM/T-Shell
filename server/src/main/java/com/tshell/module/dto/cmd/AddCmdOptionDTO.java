package com.tshell.module.dto.cmd;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * @author TheBlind
 * @date 2022/7/12
 */
public record AddCmdOptionDTO(
        @JsonProperty("channelId")
        List<String> optionNameList,
        /**
         * 描述
         */
        @JsonProperty("channelId")
        String description

) {

}
