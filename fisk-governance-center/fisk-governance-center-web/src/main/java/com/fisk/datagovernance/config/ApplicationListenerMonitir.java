package com.fisk.datagovernance.config;

import com.fisk.datagovernance.service.monitor.AccessLakeMonitorService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * @Author: wangjian
 * @Date: 2023-12-25
 * @Description:
 */

@Component
@Slf4j
public class ApplicationListenerMonitir implements CommandLineRunner {

    private static AccessLakeMonitorService accessLakeMonitorService;


    @Autowired
    public void setAccessLakeMonitorService(AccessLakeMonitorService accessLakeMonitorService) {

        ApplicationListenerMonitir.accessLakeMonitorService = accessLakeMonitorService;
    }

    @Override
    public void run(String... args) throws Exception {
        new Thread(()->{
            accessLakeMonitorService.saveCatchTargetTableRows();
        }).start();
    }
}