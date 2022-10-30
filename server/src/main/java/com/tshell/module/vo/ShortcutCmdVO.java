package com.tshell.module.vo;


import java.util.List;


/**
 * ShortcutCmdVO
 *
 */
public record ShortcutCmdVO(Integer id,
                            String name,
                            List<Integer> shortcutCmdGroupIdList,
                            List<ShortcutCmdImplVO> shortcutCmdImplList
) {

}
