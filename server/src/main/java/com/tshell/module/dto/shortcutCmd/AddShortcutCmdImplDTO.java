package com.tshell.module.dto.shortcutCmd;

import com.tshell.module.entity.ShortcutCmdImpl;

import java.util.List;

/**
 * @author TheBlind
 * @date 2022/7/12
 */
public record AddShortcutCmdImplDTO(
        String cmdTemplate,

        List<Integer> shortcutCmdTtyTypeIdList
        ) {
    public ShortcutCmdImpl convert() {
        return  ShortcutCmdImpl.builder().cmdTemplate(cmdTemplate).build();
    }
}
