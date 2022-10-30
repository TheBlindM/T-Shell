package com.tshell.module.vo;


import com.tshell.module.entity.Cmd;

import java.util.List;


/**
 * *   使用又划
 * *
 * *           ip配置
 * *
 * *  ipconfig {/all,/a}[显示完整信息]  /are 展示ipv6       ifconfig
 * *
 * *  实现  实现命令的ostype 不得设置 为父命令中的ostype
 * *
 * *             完整ip配置
 * *
 * *   ipconfig all
 * *
 * *
 * *   Git配置
 * *   主  git --version
 * *
 * *   子   remote	//查看远程库的信息
 * *       remote -v	//查看远程库的详细信息
 * *
 */

public record CmdVO(Integer id,

                    Integer parentCmdId,

                    String cmdText,

                    String describe,

                    List<Integer> cmdOsTypeIdList,

                    List<CmdOptionVO> cmdOptionList,

                    List<CmdParameterVO> cmdParameterList
) {

    public static CmdVO convert(Cmd cmd, List<Integer> cmdOsTypeIdList,
                                List<CmdParameterVO> parameterList,
                                List<CmdOptionVO> optionList) {
        return new CmdVO(cmd.id,cmd.getParentCmdId(),cmd.getCmdText(), cmd.getDescribe(),cmdOsTypeIdList,optionList,parameterList);


    }

}
