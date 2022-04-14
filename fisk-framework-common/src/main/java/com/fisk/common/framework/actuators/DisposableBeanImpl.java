package com.fisk.common.framework.actuators;


import com.fisk.common.core.utils.DateTimeUtils;
import com.fisk.common.framework.mdc.MDCHelper;
import com.fisk.common.framework.mdc.TraceTypeEnum;
import com.netflix.discovery.DiscoveryManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.stereotype.Component;

/**
 * @author gy
 */
@Component
@Slf4j
public class DisposableBeanImpl implements DisposableBean {
    @Override
    public void destroy() throws Exception {
        DiscoveryManager.getInstance().shutdownComponent();
        MDCHelper.setAppLogType(TraceTypeEnum.PROJECT_SHUTDOWN);
        log.info("------------【" + DateTimeUtils.getNow() + "】项目停止运行------------");
        MDCHelper.clear();
    }
}
