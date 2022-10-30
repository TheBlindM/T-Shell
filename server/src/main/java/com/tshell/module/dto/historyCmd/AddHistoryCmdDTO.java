package com.tshell.module.dto.historyCmd;

import cn.hutool.core.date.DateUtil;
import com.tshell.module.entity.HistoryCmd;

public record AddHistoryCmdDTO(
        String cmdText,

        String sessionId,

        /**
         * 会话类型
         */
        int sessionType
) {
    public HistoryCmd convert() {
        return HistoryCmd.builder().cmdText(this.cmdText.trim()).createTime(DateUtil.now()).sessionId(this.sessionId).build();
    }
}
