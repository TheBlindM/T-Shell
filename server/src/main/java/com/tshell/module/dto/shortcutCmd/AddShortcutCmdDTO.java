package com.tshell.module.dto.shortcutCmd;


import com.tshell.module.entity.ShortcutCmd;

import javax.validation.constraints.NotEmpty;
import java.util.List;

/**
 * 快捷命令
 */
public record AddShortcutCmdDTO(
        String name,
        @NotEmpty
        List<Integer> shortcutCmdGroupIdList,
        @NotEmpty
        List<AddShortcutCmdImplDTO> shortcutCmdImplList
) {
    public ShortcutCmd convert() {
        ShortcutCmd shortcutCmd = new ShortcutCmd();
        shortcutCmd.setName(name);
        return shortcutCmd;
    }
}
