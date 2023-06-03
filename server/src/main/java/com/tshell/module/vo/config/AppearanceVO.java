package com.tshell.module.vo.config;

import com.tshell.module.entity.Config;

/**
 * @author TheBlind
 */
public record AppearanceVO(String fontFamily,
                           Integer fontSize,
                           Integer fontWeight,
                           Integer fontWeightBold,
                           boolean enableFontLigatures,
                           String cursorShape,
                           boolean cursorBlink) {

    public static AppearanceVO convert(Config.Terminal terminal) {
        return new AppearanceVO(terminal.getFontFamily(), terminal.getFontSize(), terminal.getFontWeight(), terminal.getFontWeightBold(), terminal.isEnableFontLigatures(), terminal.getCursorShape(), terminal.isCursorBlink());
    }
}