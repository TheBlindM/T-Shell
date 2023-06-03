package com.tshell.service;

import cn.hutool.core.date.DateUtil;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tshell.module.dto.config.AppearanceDTO;
import com.tshell.module.dto.config.TerminalDTO;
import com.tshell.module.entity.Config;
import com.tshell.module.vo.config.AppearanceVO;
import com.tshell.module.vo.config.TerminalVO;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.*;

import java.io.File;
import java.nio.file.Path;
import java.util.Optional;

/**
 * @author TheBlind
 * @version 1.0
 */
@ApplicationScoped
public class ConfigService {
    final String saveRootDir = System.getProperty("user.home");
    final String fileNamePrefix = "T-Shell";
    final String fileNameSuffix = ".data";
    @Inject
    UserTransaction userTransaction;

    @Inject
    ObjectMapper objectMapper;

    public Config getSingle() {
        Optional<Config> configOpt = Config.<Config>findAll().list().stream().findFirst();
        return configOpt.orElseGet(()->{
            Config config = new Config();
            Config.Terminal terminal = new Config.Terminal();
            terminal.setCursorBlink(true);
            terminal.setCursorShape("block");
            terminal.setFontSize(20);
            terminal.setCopyOnSelect(false);
            terminal.setWordSeparator(" ()[]{}'\"");
            terminal.setEnableFontLigatures(true);
            terminal.setScrollbackLines(10210);
            config.setTerminal(terminal);
            return config;
        });
    }

    public AppearanceVO updateAppearance(AppearanceDTO appearanceDTO) throws Exception {
        Config config = getSingle();
        if (config.getId() == null) {
            userTransaction.begin();
            config.persist();
            userTransaction.commit();
        }
        userTransaction.begin();
        config = getSingle();
        appearanceDTO.convert(config.getTerminal());
        userTransaction.commit();

        return AppearanceVO.convert(config.getTerminal());
    }

    public AppearanceVO getAppearance() {
        Config config = getSingle();
        if (config == null) {
            return null;
        }
        return AppearanceVO.convert(config.getTerminal());
    }

    public TerminalVO getTerminal() {
        Config config = getSingle();
        return TerminalVO.convert(config.getTerminal());
    }

    public TerminalVO updateTerminal(TerminalDTO terminalDTO) throws Exception {
        Config config = getSingle();
        if (config.getId() == null) {
            userTransaction.begin();
            config.persist();
            userTransaction.commit();
        }
        userTransaction.begin();
        config = getSingle();
        terminalDTO.convert(config.getTerminal());
        userTransaction.commit();
        return TerminalVO.convert(config.getTerminal());
    }

    record ExportData(String version, String jsonData) {
    }

    public void exportData() {
        String time = DateUtil.now();
        String fileName = "%s-%s%s".formatted(fileNameSuffix, time, fileNameSuffix);
        File file = Path.of(saveRootDir, fileName).toFile();


    }


}
