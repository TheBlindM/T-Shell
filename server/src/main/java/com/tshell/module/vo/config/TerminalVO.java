package com.tshell.module.vo.config;

import com.tshell.module.entity.Config;

/**
 * @author TheBlind
 */
public record TerminalVO(
        Integer scrollbackLines,
        String wordSeparator,
        boolean copyOnSelect) {

    public static TerminalVO convert(Config.Terminal terminal) {
        return new TerminalVO(terminal.getScrollbackLines(),terminal.getWordSeparator(), terminal.isCopyOnSelect());
    }
}