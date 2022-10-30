package com.tshell.module.vo;


import com.tshell.module.entity.Cmd;

import java.util.List;


/**
 * 完整命令
 */
public record CompleteCmdVO(Integer id,

                            Integer parentCmdId,

                            String cmdText,

                            String describe,

                            List<Integer> cmdOsTypeIdList,

                            List<CmdOptionVO> cmdOptionList,

                            List<CmdParameterVO> cmdParameterList
) {

    public static CompleteCmdVO convert(Cmd cmd, List<Integer> cmdOsTypeIdList,
                                        List<CmdParameterVO> parameterList,
                                        List<CmdOptionVO> optionList) {
        return new CompleteCmdVO(cmd.id,cmd.getParentCmdId(),cmd.getCmdText(), cmd.getDescribe(),cmdOsTypeIdList,optionList,parameterList);


    }

}
