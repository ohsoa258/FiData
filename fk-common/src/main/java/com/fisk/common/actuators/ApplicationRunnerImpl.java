package com.fisk.common.actuators;

import com.fisk.common.mdc.MDCHelper;
import com.fisk.common.mdc.TraceTypeEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * @author gy
 */
@Component
@Slf4j
public class ApplicationRunnerImpl implements ApplicationRunner {

    @Override
    public void run(ApplicationArguments args) throws Exception {
        MDCHelper.setClass(ApplicationRunnerImpl.class.getName());
        MDCHelper.setFunction("run");
        MDCHelper.setAppLogType(TraceTypeEnum.CHARTVISUAL_START);
        log.info("------------【" + LocalDateTime.now() + "】项目开始运行------------");
    }
}
