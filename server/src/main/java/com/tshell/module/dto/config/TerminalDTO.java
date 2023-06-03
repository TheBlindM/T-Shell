package com.tshell.module.dto.config;

import com.tshell.module.entity.Config;
import com.tshell.module.vo.config.TerminalVO;

/**
 * @author TheBlind
 */
public record TerminalDTO(
        Integer scrollbackLines,
        String wordSeparator,
        boolean copyOnSelect) {

    public void convert(Config.Terminal terminal) {
        terminal.setScrollbackLines(this.scrollbackLines());
        terminal.setWordSeparator(this.wordSeparator());
        terminal.setCopyOnSelect(this.copyOnSelect());
    }
}
