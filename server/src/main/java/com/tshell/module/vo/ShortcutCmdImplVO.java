package com.tshell.module.vo;

import com.tshell.module.entity.ShortcutCmdImpl;

import java.util.List;

/**
 * @author TheBlind
 * @date 2022/7/12
 */
public record ShortcutCmdImplVO(
        String cmdTemplate,
        List<Integer> shortcutCmdTtyTypeIdList,
        List<String> shortcutCmdTtyTypeNameList
) {
    public ShortcutCmdImpl convert() {
        return ShortcutCmdImpl.builder().cmdTemplate(cmdTemplate).build();
    }
}
