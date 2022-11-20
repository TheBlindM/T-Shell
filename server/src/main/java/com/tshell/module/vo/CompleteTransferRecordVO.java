package com.tshell.module.vo;

import com.tshell.core.FileOperate;

/**
 * @author TheBlind
 * @version 1.0
 */
public record CompleteTransferRecordVO(String id,
                                       String fileName,
                                       String sessionId,
                                       String createTime,
                                       String readPath,
                                       String writePath,
                                       FileOperate operate) {
}
