package com.tshell.module.dto.cmd;

import com.tshell.module.entity.Cmd;

import java.util.List;

/**
 * @author TheBlind
 * @date 2022/6/28
 */
public record UpdCmdDto(Integer id,
                        int parentCmdId,

                        String cmd,

                        String describe,

                        List<Integer> cmdOsTypeIdList,

                        List<UpdCmdOptionDTO> cmdOptionList,

                        List<UpdCmdParameterDTO> cmdParameterList) {
    public void copyProperty(Cmd cmd) {
        cmd.setCmdText(this.cmd);
        cmd.setDescribe(describe);
        cmd.setParentCmdId(parentCmdId);
    }
}
