package com.tshell.config;

import com.tshell.core.FileManagerService;
import io.quarkus.runtime.ShutdownEvent;
import io.quarkus.runtime.StartupEvent;
import lombok.extern.slf4j.Slf4j;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

@Slf4j
@ApplicationScoped
public class AppLifecycleBean {

    @Inject
    FileManagerService fileManagerService;

    void onStart(@Observes StartupEvent ev) {
        log.info("The application is starting...");
    }

    void onStop(@Observes ShutdownEvent ev) {
        log.info("The application is stopping...");
        fileManagerService.saveAllBreakpoint();
        fileManagerService.deleteTempDir();
    }
}