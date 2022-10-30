package com.tshell.module.dto.retrieve;

import java.util.Map;

/**
 * @author TheBlind
 * @version 1.0
 * @date 2022/10/19 13:36
 */
public record ParseTemplateDTO(
        Integer id,
        String channelId,
        Map<String, String> items
) {
}
