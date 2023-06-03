package com.tshell.module.dto.config;

import com.tshell.module.entity.Config;

/**
 * @author TheBlind
 */
public record AppearanceDTO(
        String fontFamily,
        Integer fontSize,
        Integer fontWeight,
        Integer fontWeightBold,
        boolean enableFontLigatures,
        String cursorShape,
        boolean cursorBlink
) {

    public void convert(Config.Terminal terminal) {
        terminal.setCursorBlink(this.cursorBlink());
        terminal.setCursorShape(this.cursorShape);
        terminal.setFontSize(this.fontSize);
        terminal.setEnableFontLigatures(this.enableFontLigatures);
        terminal.setFontWeight(this.fontWeight);
        terminal.setFontWeightBold(this.fontWeightBold);
        terminal.setFontFamily(this.fontFamily);
    }
}
