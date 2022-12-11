package com.tshell.module.vo;

/**
 * @author TheBlind
 * @version 1.0
 */
public record TransferCountVO(
        int uploadCount,
        int downloadCount,
        int completeCount
) {
}
