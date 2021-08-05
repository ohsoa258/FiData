package com.fisk.common.actuators;


import com.fisk.common.mdc.MDCHelper;
import com.fisk.common.mdc.TraceTypeEnum;
import com.fisk.common.utils.DateTimeUtils;
import com.netflix.discovery.DiscoveryManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * @author gy
 */
@Component
@Slf4j
public class DisposableBeanImpl implements DisposableBean {
    @Override
    public void destroy() throws Exception {
        DiscoveryManager.getInstance().shutdownComponent();
        MDCHelper.setClass(DisposableBeanImpl.class.getName());
        MDCHelper.setFunction("destroy");
        MDCHelper.setAppLogType(TraceTypeEnum.PROJECT_SHUTDOWN);
        log.info("------------【" + DateTimeUtils.getNow() + "】项目停止运行------------");
    }
}
