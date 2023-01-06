package com.tshell.core;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.ConcurrentHashSet;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.watch.SimpleWatcher;
import cn.hutool.core.io.watch.WatchMonitor;
import cn.hutool.core.io.watch.watchers.DelayWatcher;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tshell.core.task.TaskExecutor;
import com.tshell.core.tty.TtyConnector;
import com.tshell.core.tty.TtyConnectorPool;
import com.tshell.module.dto.fileManager.CreateDTO;
import com.tshell.module.dto.fileManager.UploadDTO;
import com.tshell.module.entity.Breakpoint;
import com.tshell.module.entity.TransferRecord;
import com.tshell.module.vo.CompleteTransferRecordVO;
import com.tshell.module.vo.TransferCountVO;
import com.tshell.socket.WebSocket;
import lombok.extern.slf4j.Slf4j;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.transaction.Transactional;
import javax.transaction.UserTransaction;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.WatchEvent;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.stream.Stream;


/**
 * 文件管理Service
 *
 * @author TheBlind
 */
@Slf4j
@ApplicationScoped
public class FileManagerService {

    /**
     * 临时文件目录
     */
    final String tempDir = Path.of(System.getProperty("user.dir"), "temp").toString();

    WatchMonitor watchMonitor = null;
    final Map<String, TempInfo> tempFileInfoCache = HashMap.newHashMap(2);

    record TempInfo(String channelId, String readPath, String writePath) {
    }


    final TtyConnectorPool ttyConnectorPool;


    final TaskExecutor taskExecutor;


    final Set<String> pauseList = new ConcurrentHashSet<>();


    final UserTransaction userTransaction;

    final Map<String, Breakpoint> breakpointCache;


    final EntityManager entityManager;


    @Inject
    ObjectMapper objectMapper;
    final ExecutorService executorService = Executors.newVirtualThreadPerTaskExecutor();

    public FileManagerService(EntityManager entityManager, UserTransaction userTransaction, TaskExecutor taskExecutor, TtyConnectorPool ttyConnectorPool) {

        this.entityManager = entityManager;
        this.userTransaction = userTransaction;
        this.taskExecutor = taskExecutor;
        this.ttyConnectorPool = ttyConnectorPool;
        // todo 后期改为延迟加载
        List<Breakpoint> resultList = entityManager.createNativeQuery("select * from Breakpoint where current < end", Breakpoint.class).getResultList();
        if (CollUtil.isNotEmpty(resultList)) {
            breakpointCache = resultList
                    .stream()
                    .collect(Collectors.toMap(Breakpoint::getId, (breakpoint) -> breakpoint));
        } else {
            breakpointCache = HashMap.newHashMap(10);
        }
        FileUtil.mkdir(tempDir);
    }

    public void create(String channelId, CreateDTO createDTO) {
        getFileManager(channelId).create(createDTO.path(), createDTO.name(), createDTO.type());
    }

    private FileManager getFileManager(String channelId) {
        return getTyConnector(channelId).getFileManager();
    }

    private TtyConnector getTyConnector(String channelId, boolean mustBeConnected) {
        TtyConnector tyConnector = ttyConnectorPool.getConnector(channelId);
        if (mustBeConnected) {
            Assert.isTrue(tyConnector.isConnected(), "当前通道已关闭");
        }
        return tyConnector;
    }

    private TtyConnector getTyConnector(String channelId) {
        return getTyConnector(channelId, true);
    }

    public void updateContent(String channelId, String path, String readPath) {
        getFileManager(channelId).updateContent(path, readPath);
    }


    public void upload(String channelId, UploadDTO uploadDTO) {
        TtyConnector tyConnector = getTyConnector(channelId);
        FileManager fileManager = tyConnector.getFileManager();
        String sessionId = tyConnector.getSessionId();
        String separator = fileManager.getSeparator();
        uploadDTO.filePaths().forEach(filePath -> {
            String dir = uploadDTO.path();
            File file = FileUtil.file(filePath);
            var fileName = file.getName();
            if (file.isDirectory()) {
                try (Stream<Path> paths = Files.walk(file.toPath())) {
                    paths.forEach((path -> {
                        File internalFile = path.toFile();
                        String relativePath = transformFileSeparators(StrUtil.subAfter(FileUtil.getCanonicalPath(internalFile), FileUtil.getAbsolutePath(file.getParentFile()), true), separator);
                        if (!relativePath.startsWith(separator)) {
                            relativePath = separator + relativePath;
                        }
                        if (internalFile.isDirectory()) {
                            if (!relativePath.endsWith(separator)) {
                                relativePath = relativePath + separator;
                            }
                            getFileManager(channelId).create(dir, relativePath, FileType.DIRECTORY);
                        } else {
                            String completePath = dir + relativePath;
                            doUpload(TransferInfo.buildTransferInfo(completePath, FileUtil.getCanonicalPath(internalFile), sessionId, file.getTotalSpace(), FileOperate.PUT), channelId);
                        }
                    }));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

            } else {

                String completePath = dir.endsWith(separator) ? dir + fileName : dir + separator + fileName;
                doUpload(TransferInfo.buildTransferInfo(completePath, filePath, sessionId, file.getTotalSpace(), FileOperate.PUT), channelId);
            }

        });
    }


    public static String separatorsToUnix(String path) {
        return path != null && path.indexOf(92) != -1 ? path.replace('\\', '/') : path;
    }

    public static String separatorsToWindows(String path) {
        return path != null && path.indexOf(47) != -1 ? path.replace('/', '\\') : path;
    }

    public static String transformFileSeparators(String path, String separators) {
        if (path == null) {
            return null;
        } else {
            return separators.indexOf(92) != -1 ? separatorsToWindows(path) : separatorsToUnix(path);
        }
    }


    public void removeFile(String channelId, String path) {
        getFileManager(channelId).removeFile(path);
    }

    public void removeDirectory(String channelId, String path) {
        getFileManager(channelId).removeDir(path);
    }


    /**
     * 1、重命名文件或者目录
     * 2、移动文件或者目录
     */
    public void rename(String channelId, String oldPath, String fileName) {
        getFileManager(channelId).rename(oldPath, fileName);
    }

    public void download(String channelId, String path, boolean isDirectory) {

        // todo 当前处理Windows系统
        String fileName = FileUtil.getName(path);
        String savePath = Path.of(System.getProperty("user.home"), "Desktop", fileName).toString();

        TtyConnector ttyConnector = getTyConnector(channelId);
        final String sessionId = ttyConnector.getSessionId();
        FileManager fileManager = getFileManager(channelId);
        if (isDirectory) {
            fileManager.fileInfos(path).forEach(fileInfo -> {
                String relativePath = transformFileSeparators(StrUtil.subAfter(fileInfo.path(), path, true), File.separator);
                String completePath = Path.of(savePath, relativePath).toString();
                if (fileInfo.type() == FileType.DIRECTORY) {
                    FileUtil.mkdir(completePath);
                    download(channelId, fileInfo.path(), true);
                } else {
                    long size = fileManager.getSize(fileInfo.path());
                    doDownload(TransferInfo.buildTransferInfo(completePath, fileInfo.path(), sessionId, size, FileOperate.GET), channelId);
                }
            });
        } else {
            long size = fileManager.getSize(path);
            doDownload(TransferInfo.buildTransferInfo(savePath, path, sessionId, size, FileOperate.GET), channelId);
        }

    }

    public void deleteRecord(String transferRecordId) {
        TransferRecord.deleteById(transferRecordId);
        Breakpoint.delete("transferRecordId =?1", transferRecordId);
    }

    private record TransferInfo(TransferRecord transferRecord, List<Breakpoint> breakpoints) {

        @Transactional(rollbackOn = Exception.class)
        static TransferInfo buildTransferInfo(String writePath, String readPath, String sessionId, long size, FileOperate operate) {
            TransferRecord transferRecord = TransferRecord.builder()
                    .writePath(writePath)
                    .readPath(readPath)
                    .status(TransferRecord.Status.WAIT)
                    .sessionId(sessionId).operate(operate)
                    .createTime(DateUtil.now())
                    .build();
            transferRecord.persist();
            List<Breakpoint> breakpointList = List.of(new Breakpoint(transferRecord.id, 0, 0, size));
            breakpointList.forEach((breakpoint -> breakpoint.persist()));
            return new TransferInfo(transferRecord, breakpointList);
        }
    }

    private void doDownload(TransferInfo transferInfo, String channelId) {
        this.doDownload(transferInfo, channelId, null);
    }

    private void doDownload(TransferInfo transferInfo, String channelId, Runnable callBack) {

        TransferRecord transferRecord = transferInfo.transferRecord;
        List<Breakpoint> breakpoints = transferInfo.breakpoints();
        TtyConnector ttyConnector = getTyConnector(channelId);
        String sessionId = ttyConnector.getSessionId();
        FileManager fileManager = ttyConnector.getFileManager();
        String readPath = transferRecord.getReadPath();
        String writePath = transferRecord.getWritePath();
        File file = FileUtil.file(writePath);
        FileUtil.mkParentDirs(file);

        // todo  为以后 多线程做兼容
        Breakpoint breakpoint = breakpoints.get(0);
        breakpointCache.putIfAbsent(breakpoint.getId(), breakpoint);
        taskExecutor.submitUpload(sessionId, () -> fileManager.read(readPath, (inputStream) -> {
            final long[] current = new long[1];
            try (FileChannel outChannel = new RandomAccessFile(file, "rw").getChannel(); ReadableByteChannel inChannel = Channels.newChannel(inputStream)) {
                transferRecord.setStatus(TransferRecord.Status.PROCESS);
                updateTransferRecord(transferRecord);

                ByteBuffer byteBuffer = ByteBuffer.allocate(8192);
                long offset = breakpoint.getCurrent();

                while (inChannel.read(byteBuffer) != -1) {
                    byteBuffer.flip();
                    offset += outChannel.write(byteBuffer, offset);
                    current[0] = offset;
                    breakpoint.setCurrent(offset);
                    byteBuffer.clear();
                    if (pauseList.contains(transferRecord.id)) {
                        pauseList.remove(transferRecord.id);
                        log.debug("暂停 channelId{} transferRecord：{} fileName：{}  offset：{}", channelId, transferRecord.id, file.getName(), offset);
                        return;
                    }
                }
                transferRecord.setStatus(TransferRecord.Status.COMPLETE);
                updateTransferRecord(transferRecord);

                WebSocket.sendMsg(channelId, WebSocket.MsgType.TRANSFER_COMPLETE, objectMapper.writeValueAsString(new Progress(100.0, channelId, file.getName(), TransferRecord.Status.COMPLETE, transferRecord.id, FileOperate.GET, transferRecord.getReadPath(), transferRecord.getWritePath())));

            } catch (IOException e) {
                log.error("doUpload  channelId{} transferRecord：{} fileName：{}  offset：{} error:{}", channelId, transferRecord.id, file.getName(), current[0], e);
            } finally {
                updateBreakpoint(breakpoint.getId());
                if (Objects.nonNull(callBack)) {
                    callBack.run();
                }
            }

        }, breakpoint.getCurrent()));
    }


    private void doUpload(TransferInfo transferInfo, String channelId) {
        TransferRecord transferRecord = transferInfo.transferRecord;
        List<Breakpoint> breakpoints = transferInfo.breakpoints();
        String sessionId = getTyConnector(channelId).getSessionId();
        String readPath = transferRecord.getReadPath();
        String writePath = transferRecord.getWritePath();


        // todo  为以后 多线程做兼容
        Breakpoint breakpoint = breakpoints.get(0);
        breakpointCache.putIfAbsent(breakpoint.getId(), breakpoint);

        taskExecutor.submitUpload(sessionId, () -> getFileManager(channelId).upload(writePath, (outputStream) -> {
            File file = FileUtil.file(readPath);

            final long[] current = new long[1];
            try (FileChannel inChannel = FileChannel.open(file.toPath()); WritableByteChannel outChannel = Channels.newChannel(outputStream)) {
                transferRecord.setStatus(TransferRecord.Status.PROCESS);
                updateTransferRecord(transferRecord);

                ByteBuffer byteBuffer = ByteBuffer.allocate(8192);
                long offset = breakpoint.getCurrent();
                while (inChannel.read(byteBuffer) != -1) {
                    byteBuffer.flip();
                    offset += outChannel.write(byteBuffer);
                    current[0] = offset;
                    breakpoint.setCurrent(offset);
                    byteBuffer.clear();
                    if (pauseList.contains(transferRecord.id)) {
                        pauseList.remove(transferRecord.id);
                        log.debug("暂停 channelId{} transferRecord：{} fileName：{}  offset：{}", channelId, transferRecord.id, transferRecord.getWritePath(), offset);
                        return;
                    }

                }
                transferRecord.setStatus(TransferRecord.Status.COMPLETE);
                updateTransferRecord(transferRecord);


                WebSocket.sendMsg(channelId, WebSocket.MsgType.TRANSFER_COMPLETE, objectMapper.writeValueAsString(new Progress(100.0, channelId, file.getName(), TransferRecord.Status.COMPLETE, transferRecord.id, FileOperate.PUT, transferRecord.getReadPath(), transferRecord.getWritePath())));
            } catch (IOException e) {
                log.error("doUpload  channelId{} transferRecord：{} fileName：{}  offset：{} error:{}", channelId, transferRecord.id, transferRecord.getWritePath(), current[0], e);
            } finally {
                updateBreakpoint(breakpoint.getId());
            }
        }, breakpoint.getCurrent()));
    }


    private void updateTransferRecord(TransferRecord transferRecord) {
        try {
            userTransaction.begin();
            TransferRecord.update("status = ?1 where id = ?2", transferRecord.getStatus(), transferRecord.id);

            userTransaction.commit();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    private void updateBreakpoint(String breakpointId) {
        try {
            userTransaction.begin();
            long offset = breakpointCache.get(breakpointId).getCurrent();
            Breakpoint breakpoint = Breakpoint.<Breakpoint>findById(breakpointId);
            breakpoint.setCurrent(offset);
            userTransaction.commit();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Transactional(rollbackOn = Exception.class)
    public void pauseTransfer(String channelId) {
        TtyConnector ttyConnector = getTyConnector(channelId, false);
        String sessionId = ttyConnector.getSessionId();

        List<TransferRecord> transferRecords = TransferRecord.<TransferRecord>list("sessionId =?1 and status =?2 ", sessionId, TransferRecord.Status.PROCESS);
        // 暂停当前session 下所有进行中的文件操作
        if (!transferRecords.isEmpty()) {
            ArrayList<String> ids = new ArrayList<>(transferRecords.size());
            transferRecords.stream().forEach(transferRecord -> {
                ids.add(transferRecord.id);
                transferRecord.setStatus(TransferRecord.Status.PAUSE);
            });
            pauseList.addAll(ids);
        }


    }

    public void pauseTransfer(String channelId, String transferRecordId) {
        if (pauseList.add(transferRecordId)) {
            TransferRecord transferRecord = TransferRecord.<TransferRecord>findByIdOptional(transferRecordId).orElseThrow(() -> new IllegalArgumentException("没有该id"));
            transferRecord.setStatus(TransferRecord.Status.PAUSE);
        }
    }

    public void continueTransfer(String channelId, String transferRecordId) {
        TransferRecord transferRecord = TransferRecord.<TransferRecord>findByIdOptional(transferRecordId).orElseThrow(() -> new IllegalArgumentException("没有该id"));
        FileOperate operate = transferRecord.getOperate();


        List<Breakpoint> breakpointList = getBreakpointList(transferRecordId);
        TransferInfo transferInfo = new TransferInfo(transferRecord, breakpointList);
        pauseList.remove(transferRecordId);
        if (operate == FileOperate.PUT) {
            doUpload(transferInfo, channelId);
        } else {
            doDownload(transferInfo, channelId);
        }
    }


    public List<FileInfo> fileInfos(String channelId, String path) {
        FileManager fileManager = getFileManager(channelId);
        String separator = fileManager.getSeparator();
        if (path == null || "null".equals(path)) {
            path = separator;
        }
        String completePath = path.endsWith(separator) ? path : path + separator;
        List<FileInfo> fileInfos = fileManager.fileInfos(completePath);
        return fileInfos.stream().sorted(Comparator.comparing(FileInfo::type)).toList();
    }


    public List<CompleteTransferRecordVO> getCompleteList(String channelId) {
        TtyConnector ttyConnector = getTyConnector(channelId);
        String sessionId = ttyConnector.getSessionId();
        return TransferRecord.<TransferRecord>list("sessionId =?1 and status =?2 order by createTime desc", sessionId, TransferRecord.Status.COMPLETE).stream().map((transferRecord) -> new CompleteTransferRecordVO(transferRecord.getId(), FileUtil.getName(transferRecord.getReadPath()), transferRecord.getSessionId(), transferRecord.getCreateTime(), transferRecord.getReadPath(), transferRecord.getWritePath(), transferRecord.getOperate())).toList();
    }


    public List<Progress> getUploadList(String channelId) {
        return getProgresses(channelId, FileOperate.PUT);
    }


    private List<Breakpoint> getBreakpointList(String transferRecordId) {
        //return Breakpoint.<Breakpoint>list("transferRecordId =?1", transferRecordId);
        return breakpointCache.values().stream().filter((breakpoint -> breakpoint.getTransferRecordId().equals(transferRecordId))).collect(Collectors.toList());
    }


    public List<Progress> getDownloadList(String channelId) {
        return getProgresses(channelId, FileOperate.GET);
    }


    public record Progress(double percent, String channelId, String fileName, TransferRecord.Status status,
                           String transferRecordId, FileOperate operate, String readPath, String writePath) {
        public Progress(double percent, String channelId, String fileName, TransferRecord.Status status, String transferRecordId, FileOperate operate, String readPath, String writePath) {
            this.percent = percent;
            this.channelId = channelId;
            this.fileName = fileName;
            this.status = status;
            this.transferRecordId = transferRecordId;
            this.operate = operate;
            this.readPath = readPath;
            this.writePath = writePath;
        }

        public Progress(double percent, String channelId, String fileName, TransferRecord.Status status, String transferRecordId) {
            this(percent, channelId, fileName, status, transferRecordId, null, null, null);
        }
    }

    private List<Progress> getProgresses(String channelId, FileOperate fileOperate) {
        TtyConnector ttyConnector = getTyConnector(channelId);
        String sessionId = ttyConnector.getSessionId();
        TransferRecord.Status status = TransferRecord.Status.COMPLETE;

        List<TransferRecord> transferRecords = TransferRecord.<TransferRecord>list("sessionId =?1 and status !=?2 and operate = ?3", sessionId, status, fileOperate);
        List<Progress> list = transferRecords.stream().map(transferRecord -> {
            List<Breakpoint> breakpoints = getBreakpointList(transferRecord.id);

            Breakpoint breakpoint = breakpoints.get(0);
            double percent = (double) breakpoint.getCurrent() / breakpoint.getEnd() * 100.0;
            return new Progress(NumberUtil.round(percent, 2).doubleValue(), channelId, FileUtil.getName(transferRecord.getWritePath()), transferRecord.getStatus(), transferRecord.id);
        }).collect(Collectors.toList());
        return list;
    }

    public void saveAllBreakpoint() {
        breakpointCache.values().stream().forEach((breakpoint -> {
            updateBreakpoint(breakpoint.getId());
        }));

    }

    public TransferCountVO getTransferTaskCount(String channelId) {
        TtyConnector ttyConnector = getTyConnector(channelId);
        String sessionId = ttyConnector.getSessionId();
        long uploadCount = TransferRecord.count("sessionId =?1 and status !=?2 and operate = ?3", sessionId, TransferRecord.Status.COMPLETE, FileOperate.PUT);
        long downloadCount = TransferRecord.count("sessionId =?1 and status !=?2 and operate = ?3", sessionId, TransferRecord.Status.COMPLETE, FileOperate.GET);
        long completeCount = TransferRecord.count("sessionId =?1 and status =?2", sessionId, TransferRecord.Status.COMPLETE);
        return new TransferCountVO((int) uploadCount, (int) downloadCount, (int) completeCount);
    }


    public void openFile(String channelId, String path) {

        String fileName = FileUtil.getName(path);

        TtyConnector tyConnector = getTyConnector(channelId);
        FileManager fileManager = tyConnector.getFileManager();

        String id = "%s-%s".formatted(tyConnector.getSessionId(), pathEncode(path));
        String savePath = Path.of(tempDir, id).toString();

        tempFileInfoCache.remove(id);
        long size = fileManager.getSize(path);

        executorService.execute(() -> {
            fileManager.read(path, (inputStream) -> {

                try (ReadableByteChannel inChannel = Channels.newChannel(inputStream); WritableByteChannel outChannel = Channels.newChannel(new FileOutputStream(savePath))) {

                    ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
                    long offset = 0;
                    while (inChannel.read(byteBuffer) != -1) {
                        byteBuffer.flip();
                        offset += outChannel.write(byteBuffer);
                        byteBuffer.clear();
                        double percent = (double) offset / size * 100.0;
                        Progress progress = new Progress(NumberUtil.round(percent, 2).doubleValue(), channelId, fileName, TransferRecord.Status.PROCESS, id);
                        WebSocket.sendIntervalMsg(channelId, WebSocket.MsgType.OPEN_FILE_PROGRESS, objectMapper.writeValueAsString(progress), id);
                        if (pauseList.remove(id)) {
                            return;
                        }

                    }
                    WebSocket.sendMsg(channelId, WebSocket.MsgType.OPEN_FILE_PROGRESS, objectMapper.writeValueAsString(new Progress(100.00, channelId, fileName, TransferRecord.Status.COMPLETE, id)));
                    WebSocket.sendMsg(channelId, WebSocket.MsgType.OPEN_FILE, savePath);

                    tempFileInfoCache.put(id, new TempInfo(channelId, savePath, path));
                    watchTempFile();
                } catch (IOException e) {
                    log.error("openFile  channelId{}  fileName：{}  error:{}", channelId, fileName, e);
                }
            });
        });

    }


    public void cancelOpenFile(String id) {
        pauseList.add(id);
    }


    public void watchTempFile() {

        if (watchMonitor != null) {
            return;
        }
        // 监听文件修改
        watchMonitor = WatchMonitor.create(new File(tempDir), WatchMonitor.ENTRY_MODIFY);

        // 设置钩子函数
        watchMonitor.setWatcher(new DelayWatcher(new SimpleWatcher() {
            @Override
            public void onModify(WatchEvent<?> event, Path currentPath) {
                if (tempFileInfoCache.containsKey(event.context().toString())) {
                    if (log.isDebugEnabled()) {
                        log.debug("修改事件：path：{}", event.context().toString());
                    }
                    var temInfo = tempFileInfoCache.get(event.context().toString());
                    updateContent(temInfo.channelId(), temInfo.writePath(), temInfo.readPath());
                }
            }
        }, 500));

        // 设置监听目录的最大深入，目录层级大于制定层级的变更将不被监听，默认只监听当前层级目录
        watchMonitor.setMaxDepth(0);
        // 启动监听
        watchMonitor.start();

    }

    public void deleteTempDir() {
        FileUtil.del(tempDir);
    }

    public String pathEncode(String path) {
        return path.indexOf(47) != -1 ? path.replace('/', '%') : path.replace('\\', '%');
    }
}
