package com.tshell.module.dto.historyCmd;

import com.tshell.config.CustomDateDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import java.time.LocalDate;

public record GetHistoryCmdPageDTO(
        String sessionId,

        @JsonDeserialize(using = CustomDateDeserializer.class)
        LocalDate startDate,
        @JsonDeserialize(using = CustomDateDeserializer.class)
        LocalDate endDate
) {
}
