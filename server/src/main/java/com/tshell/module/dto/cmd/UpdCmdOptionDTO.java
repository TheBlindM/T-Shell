package com.tshell.module.dto.cmd;

import java.util.List;

/**
 * @author TheBlind
 * @date 2022/7/12
 */
public record UpdCmdOptionDTO(
        Integer id,
        List<String> optionNameList,
        /**
         * 描述
         */
        String description

) {

}
