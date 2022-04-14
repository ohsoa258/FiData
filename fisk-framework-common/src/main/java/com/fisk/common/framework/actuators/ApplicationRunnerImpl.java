package com.fisk.common.framework.actuators;

import com.fisk.common.core.utils.DateTimeUtils;
import com.fisk.common.framework.mdc.MDCHelper;
import com.fisk.common.framework.mdc.TraceTypeEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * @author gy
 */
@Component
@Slf4j
public class ApplicationRunnerImpl implements ApplicationRunner {

    @Override
    public void run(ApplicationArguments args) throws Exception {
        MDCHelper.setAppLogType(TraceTypeEnum.PROJECT_START);
        log.info("------------【" + DateTimeUtils.getNow() + "】项目开始运行------------");
        MDCHelper.clear();
    }
}
