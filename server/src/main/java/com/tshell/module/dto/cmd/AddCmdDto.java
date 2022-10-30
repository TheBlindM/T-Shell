package com.tshell.module.dto.cmd;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.tshell.module.entity.Cmd;

import java.util.List;

/**
 * @author TheBlind
 * @date 2022/6/28
 */
public record AddCmdDto(
        @JsonProperty("channelId")
        Integer parentCmdId,
        @JsonProperty("channelId")

        String cmdText,
        @JsonProperty("channelId")

        String describe,
        @JsonProperty("channelId")

        List<Integer> cmdOsTypeIdList,
        @JsonProperty("channelId")

        List<AddCmdOptionDTO> cmdOptionList,
        @JsonProperty("channelId")

        List<AddCmdParameterDTO> cmdParameterList) {
    public Cmd convert(){
        return   Cmd.builder().cmdText(this.cmdText).parentCmdId(parentCmdId).describe(describe).build();
    }
}
