package com.tshell.service;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.tshell.core.TerminalService;
import com.tshell.core.client.TtyType;
import com.tshell.module.dto.retrieve.ParseTemplateDTO;
import com.tshell.module.entity.*;
import com.tshell.module.vo.CmdOptionVO;
import com.tshell.module.vo.CmdParameterVO;
import com.tshell.module.vo.RetrieveMenuVO;
import com.tshell.module.vo.RetrieveVO;
import com.tshell.utils.PlaceholderResolver;
import lombok.extern.slf4j.Slf4j;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

/**
 * @author TheBlind
 * @date 2022/7/14
 */
@ApplicationScoped
@Slf4j
public class RetrieveService {

    @Inject
    CmdService cmdService;
    @Inject
    HistoryCmdService historyCmdService;
    @Inject
    ShortcutCmdService shortcutCmdService;

    @Inject
    VariableService variableService;


    @Inject
    TerminalService terminalService;


    /**
     * 检索
     */

    public RetrieveMenuVO retrieve(String item, String channelId, boolean skipVerify) {

        String trimmedItem = item.strip();
        List<RetrieveVO> retrieveVOList;
        boolean isInCommandInput = skipVerify ? true : terminalService.isInCommandInput(channelId);
        log.debug("isInCommandInput:{}", isInCommandInput);
        log.debug("trimmedItem:{}", trimmedItem);
        if (isInCommandInput) {
            TtyType ttyOsType = terminalService.getTtyOsType(channelId);
            log.debug("ttyOsType {} {}", ttyOsType.getId(), ttyOsType.getName());
            //CompletableFuture<List<RetrieveVO>> cmdListFuture = CompletableFuture.supplyAsync(() -> retrieveCmd(trimmedItem, 0, ttyOsType.getId()));
            CompletableFuture<List<RetrieveVO>> shortcutCmdListFuture = CompletableFuture.supplyAsync(() -> retrieveShortcutCmd(trimmedItem, ttyOsType.getId()));
            CompletableFuture<List<RetrieveVO>> historyCmdListFuture = CompletableFuture.supplyAsync(() -> retrieveHistoryCmd(trimmedItem, ttyOsType.getId()));


            try {
                CompletableFuture.allOf(shortcutCmdListFuture, historyCmdListFuture).join();
                List<RetrieveVO> retrieveShortcutCmdList = shortcutCmdListFuture.get();
                List<RetrieveVO> retrieveHistoryCmdList = historyCmdListFuture.get();
                ArrayList<RetrieveVO> list = new ArrayList<>(retrieveShortcutCmdList.size() + retrieveHistoryCmdList.size());

                list.addAll(retrieveShortcutCmdList);
                list.addAll(retrieveHistoryCmdList);
                retrieveVOList = list;
            } catch (InterruptedException | ExecutionException e) {
                throw new RuntimeException(e);
            }
        } else {
            retrieveVOList = Collections.emptyList();
        }
        return new RetrieveMenuVO(isInCommandInput, retrieveVOList);
    }


    public List<RetrieveVO> retrieveCmd(String item, Integer currentCmdId, Integer osTypeId) {


        List<Cmd> cmdList = cmdService.getListByOsTypeIdAndCmdText(osTypeId, item);

        // 进入子命令
        if (currentCmdId != 0) {
            List<Parameter> parameterList = Parameter.<Parameter>list("cmdId = ?1", currentCmdId);
            List<CmdParameterVO> cmdParameterVOList = parameterList.stream().map(parameter -> new CmdParameterVO(parameter.id, parameter.getIndex(), parameter.getDescription())).collect(Collectors.toUnmodifiableList());

            List<Option> optionList = Option.<Option>list("cmdId = ?1", currentCmdId);
            List<CmdOptionVO> cmdOptionVOList = optionList.stream().map(option -> new CmdOptionVO(option.id, JSONUtil.toList(option.getJsonNames(), String.class), option.getDescription())).collect(Collectors.toUnmodifiableList());

        }
        return cmdList.stream().map(cmd -> createRetrieve(cmd.getCmdText(), String.valueOf(cmd.id), RetrieveItemType.CMD)).collect(Collectors.toUnmodifiableList());
    }


    /**
     * 检索快捷命令
     *
     * @param item      检索项
     * @param ttyTypeId
     * @return
     */
    public List<RetrieveVO> retrieveShortcutCmd(String item, Integer ttyTypeId) {
        List<ShortcutCmd> shortcutCmdList = shortcutCmdService.getShortcutCmdByNameAndOsTypeId(item, ttyTypeId);
        log.debug("shortcutCmdList {}", shortcutCmdList.size());
        return shortcutCmdList.stream().map(cmd -> createRetrieve(cmd.getName(), String.valueOf(cmd.id), RetrieveItemType.SHORTCUT_CMD)).toList();
    }

    public List<RetrieveVO> retrieveHistoryCmd(String item, Integer osTypeId) {
        List<HistoryCmd> shortcutCmdList = historyCmdService.getHistoryCmdByNameAndOsTypeId(item, osTypeId);
        log.debug("retrieveHistoryCmd {}", shortcutCmdList.size());
        return shortcutCmdList.stream().map(historyCmd -> createRetrieve(historyCmd.getCmdText(), String.valueOf(historyCmd.getId()), RetrieveItemType.HISTORY_CMD)).toList();
    }

    public Map<String, String> getMatchItems(Integer id, String channelId) throws Exception {
        TtyType ttyOsType = terminalService.getTtyOsType(channelId);
        String cmdTemplate = shortcutCmdService.getShortcutCmdByIdAndOsTypeId(id, ttyOsType.getId());
        Map<String, String> matchItems = variableService.getMatchItems(cmdTemplate);
        if (matchItems.isEmpty()) {
            parseTemplate(new ParseTemplateDTO(id, channelId, Collections.emptyMap()));
        }
        return matchItems;
    }


    public void parseTemplate(ParseTemplateDTO parseTemplateDTO) throws Exception {
        Integer id = parseTemplateDTO.id();
        String channelId = parseTemplateDTO.channelId();
        Map<String, String> items = parseTemplateDTO.items();
        TtyType ttyOsType = terminalService.getTtyOsType(channelId);
        ShortcutCmd shortcutCmd = ShortcutCmd.<ShortcutCmd>findById(id);
        String shortcutCmdName = shortcutCmd.getName();

        String cmdTemplate = shortcutCmdService.getShortcutCmdByIdAndOsTypeId(id, ttyOsType.getId());
        AtomicBoolean whole = new AtomicBoolean(true);
        String value = items.isEmpty() ? cmdTemplate : getResolveByRule(items, cmdTemplate, whole);

        String delete = StrUtil.repeatAndJoin("\b", shortcutCmdName.length(), null);

        terminalService.executeCmd(channelId, delete);
        // 如果全部匹配成功则 自动执行
        if (whole.get()) {
            value += "\n";
        }

        terminalService.executeCmd(channelId, value);
    }

    private static String getResolveByRule(Map<String, String> items, String cmdTemplate, AtomicBoolean whole) {
        return PlaceholderResolver.getDefaultResolver().resolveByRule(cmdTemplate, (var) -> {
            Optional<String> valueByVarName = Optional.ofNullable(items.get(var));
            if (!valueByVarName.isPresent()) {
                whole.set(false);
            }
            return valueByVarName.orElse("");
        });
    }


    public RetrieveVO createRetrieve(String item, String id, RetrieveItemType itemType) {
        return new RetrieveVO(item, id, itemType);
    }


    public enum RetrieveItemType {
        /**
         * 命令
         */
        CMD(1),
        /**
         * 快捷命令
         */
        SHORTCUT_CMD(2),
        /**
         * 历史命令
         */
        HISTORY_CMD(3);

        public final int code;


        RetrieveItemType(int code) {
            this.code = code;
        }

        static RetrieveItemType getType(String packValue) {
            RetrieveItemType retrieveItemType = RetrieveItemType.valueOf(packValue.substring(0, 1));

            return retrieveItemType;
        }

        Integer decode(String value) {
            return Integer.parseInt(value.substring(1));
        }

        String encode(String value) {
            return code + value;
        }
    }


}
