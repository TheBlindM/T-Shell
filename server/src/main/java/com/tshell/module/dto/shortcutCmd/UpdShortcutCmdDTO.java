package com.tshell.module.dto.shortcutCmd;


import com.tshell.module.entity.ShortcutCmd;

import java.util.List;

/**
 * 快捷命令
 */
public record UpdShortcutCmdDTO(
        Integer id,
        String name,
        List<Integer> shortcutCmdGroupIdList,
        List<UpdShortcutCmdImplDTO> shortcutCmdImplList
) {
    public void copyProperty(ShortcutCmd shortcutCmd) {
        shortcutCmd.setName(name);
    }
    public ShortcutCmd convert() {
        ShortcutCmd shortcutCmd = new ShortcutCmd();
        shortcutCmd.setName(name);
        return shortcutCmd;
    }
}
