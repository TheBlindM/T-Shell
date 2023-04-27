package com.tshell.core.task;

import cn.hutool.core.thread.ThreadUtil;
import com.tshell.core.FileOperate;
import lombok.extern.slf4j.Slf4j;

import jakarta.enterprise.context.ApplicationScoped;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * @author TheBlind
 */
@Slf4j
@ApplicationScoped
public class TaskExecutor {

    final ExecutorService executorService = Executors.newVirtualThreadPerTaskExecutor();


    public TaskExecutor() {
        startWorking();
    }

    private void startWorking() {
        executorService.execute(() -> {
            while (true) {
                sessionTaskQueue.forEach((key, value) -> {
                    var countDownLatch = channelCountDownLatch.computeIfAbsent(key, (k) -> new CountDownLatch(defaultChannelThreadCount));
                    try {
                        countDownLatch.await(0, TimeUnit.SECONDS);
                    } catch (InterruptedException e) {
                        log.debug("{} 获取失败  count {}", key, countDownLatch.getCount());
                        return;
                    }
                    if (value.size() != 0) {
                        executorService.execute(() -> {
                            value.removeFirst().run();
                            countDownLatch.countDown();
                        });
                    }

                });
                ThreadUtil.sleep(2, TimeUnit.SECONDS);
            }
        });
    }

    /**
     * channelCountDownLatch
     * <p>
     * 防止 并发导致链接过多
     */
    Map<String, CountDownLatch> channelCountDownLatch = new HashMap<>();

    final int defaultChannelThreadCount = 3;

    final Map<String, LinkedList<Runnable>> sessionTaskQueue = new HashMap<>();


    public void submit(String sessionId, FileOperate fileOperate, Runnable runnable) {
        LinkedList<Runnable> runnables = sessionTaskQueue.computeIfAbsent(createKey(sessionId, fileOperate), (key) -> new LinkedList<>());
        runnables.add(runnable);
    }

    private String createKey(String sessionId, FileOperate fileOperate) {
        return "%s-%s".formatted(sessionId, fileOperate.name());
    }

    public void submitDownload(String sessionId, Runnable runnable) {
        this.submit(sessionId, FileOperate.GET, runnable);
    }

    public void submitUpload(String sessionId, Runnable runnable) {
        this.submit(sessionId, FileOperate.PUT, runnable);
    }

    public int sessionTaskCount(String sessionId, FileOperate fileOperate) {
        return sessionTaskQueue.get(createKey(sessionId, FileOperate.GET)).size();
    }

    public int sessionTaskCount(String sessionId) {
        return sessionTaskCount(sessionId, FileOperate.PUT) + sessionTaskCount(sessionId, FileOperate.GET);
    }


}
