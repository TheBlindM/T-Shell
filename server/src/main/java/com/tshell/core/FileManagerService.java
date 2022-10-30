package com.tshell.core;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.ConcurrentHashSet;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.NumberUtil;
import com.tshell.core.task.TaskExecutor;
import com.tshell.core.tty.TtyConnector;
import com.tshell.core.tty.TtyConnectorPool;
import com.tshell.module.dto.fileManager.CreateDTO;
import com.tshell.module.dto.fileManager.UploadDTO;
import com.tshell.module.entity.Breakpoint;
import com.tshell.module.entity.TransferRecord;
import lombok.extern.slf4j.Slf4j;

import javax.enterprise.context.ApplicationScoped;
import javax.persistence.EntityManager;
import javax.transaction.Transactional;
import javax.transaction.UserTransaction;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;



/**
 * 文件管理Service
 *
 * @author TheBlind
 */
@Slf4j
@ApplicationScoped
public class FileManagerService {


    final TtyConnectorPool ttyConnectorPool;


    final TaskExecutor taskExecutor;


    final Set<String> pauseList = new ConcurrentHashSet<>();


    final UserTransaction userTransaction;

    final Map<String, Breakpoint> breakpointCache;


    final EntityManager entityManager;

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
    }

    public void create(String channelId, CreateDTO createDTO) {
        getFileManager(channelId).create(createDTO.path(), createDTO.name(), createDTO.type());
    }

    private FileManager getFileManager(String channelId) {
        return getTyConnector(channelId).getFileManager();
    }

    private TtyConnector getTyConnector(String channelId) {
        return ttyConnectorPool.getConnector(channelId);
    }

    public void updateContent(String channelId, String path, String content) {
        getFileManager(channelId).updateContent(path, content);
    }


    public void upload(String channelId, UploadDTO uploadDTO) {
        String sessionId = getTyConnector(channelId).getSessionId();
        uploadDTO.filePaths().forEach(filePath -> {
            File file = FileUtil.file(filePath);
            doUpload(TransferInfo.buildTransferInfo(uploadDTO.path(), filePath, sessionId, file.getName(), file.getTotalSpace(), FileOperate.PUT), channelId);
        });
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

    public void download(String channelId, String path) {

        // todo 当前处理Windows系统
        String fileName = FileUtil.getName(path);
        String savePath = Path.of(System.getProperty("user.home"),"Desktop",fileName).toString();

        TtyConnector ttyConnector = getTyConnector(channelId);
        final String sessionId = ttyConnector.getSessionId();
        FileManager fileManager = getFileManager(channelId);
        long size = fileManager.getSize(path);
        doDownload(TransferInfo.buildTransferInfo(savePath, path, sessionId, fileName, size, FileOperate.GET), channelId);
    }

    public void deleteRecord(String transferRecordId) {
        TransferRecord.deleteById(transferRecordId);
        Breakpoint.delete("transferRecordId =?1", transferRecordId);
    }

    private record TransferInfo(TransferRecord transferRecord, List<Breakpoint> breakpoints) {

        @Transactional(rollbackOn = Exception.class)
        static TransferInfo buildTransferInfo(String writePath, String readPath, String sessionId, String fileName, long size, FileOperate operate) {
            TransferRecord transferRecord = TransferRecord.builder()
                    .writePath(writePath)
                    .readPath(readPath)
                    .status(TransferRecord.Status.WAIT)
                    .sessionId(sessionId).operate(operate)
                    .fileName(fileName)
                    .createTime(DateUtil.now())
                    .build();
            transferRecord.persist();
            List<Breakpoint> breakpointList = List.of(new Breakpoint(transferRecord.id, 0, 0, size));
            breakpointList.forEach((breakpoint -> breakpoint.persist()));
            return new TransferInfo(transferRecord, breakpointList);
        }
    }


    private void doDownload(TransferInfo transferInfo, String channelId) {

        TransferRecord transferRecord = transferInfo.transferRecord;
        List<Breakpoint> breakpoints = transferInfo.breakpoints();
        TtyConnector ttyConnector = getTyConnector(channelId);
        String sessionId = ttyConnector.getSessionId();
        FileManager fileManager = ttyConnector.getFileManager();
        String path = transferRecord.getReadPath();
        String writePath = transferRecord.getWritePath();
        File file = FileUtil.file(writePath);

        // todo  为以后 多线程做兼容
        Breakpoint breakpoint = breakpoints.get(0);
        breakpointCache.putIfAbsent(breakpoint.getId(), breakpoint);
        taskExecutor.submitUpload(sessionId, () -> fileManager.read(path, (inputStream) -> {
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
                        log.debug("暂停 channelId{} transferRecord：{} fileName：{}  offset：{}", channelId, transferRecord.id, transferRecord.getFileName(), offset);
                        return;
                    }
                }
                transferRecord.setStatus(TransferRecord.Status.COMPLETE);
                updateTransferRecord(transferRecord);
            } catch (IOException e) {
                log.error("doUpload  channelId{} transferRecord：{} fileName：{}  offset：{} error:{}", channelId, transferRecord.id, transferRecord.getFileName(), current[0], e);
            } finally {
                updateBreakpoint(breakpoint.getId());
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
        taskExecutor.submitUpload(sessionId, () -> getFileManager(channelId).upload(writePath, transferRecord.getFileName(), (outputStream) -> {
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
                        log.debug("暂停 channelId{} transferRecord：{} fileName：{}  offset：{}", channelId, transferRecord.id, transferRecord.getFileName(), offset);
                        return;
                    }

                }
                transferRecord.setStatus(TransferRecord.Status.COMPLETE);
                updateTransferRecord(transferRecord);
            } catch (IOException e) {
                log.error("doUpload  channelId{} transferRecord：{} fileName：{}  offset：{} error:{}", channelId, transferRecord.id, transferRecord.getFileName(), current[0], e);
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
        TtyConnector ttyConnector = getTyConnector(channelId);
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
        List<FileInfo> fileInfos = getFileManager(channelId).fileInfos(path);
        return fileInfos.stream().sorted(Comparator.comparing(FileInfo::type)).toList();
    }


    public List<TransferRecord> getCompleteList(String channelId) {
        TtyConnector ttyConnector = getTyConnector(channelId);
        String sessionId = ttyConnector.getSessionId();
        return TransferRecord.<TransferRecord>list("sessionId =?1 and status =?2", sessionId, TransferRecord.Status.COMPLETE);
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
                           String transferRecordId) {
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
            return new Progress(NumberUtil.round(percent, 2).doubleValue(), channelId, transferRecord.getFileName(), transferRecord.getStatus(), transferRecord.id);
        }).collect(Collectors.toList());
        return list;
    }

    public void saveAllBreakpoint() {
        breakpointCache.values().stream().forEach((breakpoint -> {
            updateBreakpoint(breakpoint.getId());
        }));

    }

    public int getTransferCount(String channelId) {
        TtyConnector ttyConnector = getTyConnector(channelId);
        String sessionId = ttyConnector.getSessionId();
        return taskExecutor.sessionTaskCount(sessionId);
    }

}
