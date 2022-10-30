package com.tshell.module.vo;

import java.util.List;

/**
 * @author TheBlind
 * @date 2022/7/12
 */
public record CmdOptionVO(
        Integer id,
        List<String> optionNameList,
        /**
         * 描述
         */
        String description

) {

}
