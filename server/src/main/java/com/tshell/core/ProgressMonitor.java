package com.tshell.core;

public interface ProgressMonitor {
    boolean call(Progress progress);

    void end(int sessionId, Progress progress);

    public record Progress(FileOperate op, double percent, String channelId, String fromPath, String toPath,
                           String fileName, String key) {
    }
}
