package com.tshell.core;

/**
 * @author TheBlind
 * @date 2022/7/9
 */
public record TtySize(
        int columns,
        int lines,
        int width,
        int height
) {
}
