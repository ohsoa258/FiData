package com.fisk.common.framework.hystrix;

import com.netflix.hystrix.strategy.HystrixPlugins;
import com.netflix.hystrix.strategy.concurrency.HystrixConcurrencyStrategy;
import com.netflix.hystrix.strategy.eventnotifier.HystrixEventNotifier;
import com.netflix.hystrix.strategy.executionhook.HystrixCommandExecutionHook;
import com.netflix.hystrix.strategy.metrics.HystrixMetricsPublisher;
import com.netflix.hystrix.strategy.properties.HystrixPropertiesStrategy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

/**
 * @author gy
 * @version 1.0
 * @description Hystrix配置
 * @date 2022/12/14 14:48
 */
@Configuration
@Slf4j
public class HystrixConfiguration {

    /**
     * 加载编写的自定义Hystrix插件
     * 由于Spring Boot也会注册Hystrix插件，Hystrix不允许重复注册。
     * 所以要在注册之前清空一下，防止重复注册
     */
    @PostConstruct
    public void init() {
        try {
            HystrixConcurrencyStrategy target = new MdcHystrixConcurrencyStrategy();
            HystrixConcurrencyStrategy strategy = HystrixPlugins.getInstance().getConcurrencyStrategy();
            if (strategy instanceof MdcHystrixConcurrencyStrategy) {
                // Welcome to singleton hell...
                return;
            }
            HystrixCommandExecutionHook commandExecutionHook = HystrixPlugins
                    .getInstance().getCommandExecutionHook();
            HystrixEventNotifier eventNotifier = HystrixPlugins.getInstance()
                    .getEventNotifier();
            HystrixMetricsPublisher metricsPublisher = HystrixPlugins.getInstance()
                    .getMetricsPublisher();
            HystrixPropertiesStrategy propertiesStrategy = HystrixPlugins.getInstance()
                    .getPropertiesStrategy();

            if (log.isDebugEnabled()) {
                log.debug("Current Hystrix plugins configuration is ["
                        + "concurrencyStrategy [" + target + "]," + "eventNotifier ["
                        + eventNotifier + "]," + "metricPublisher [" + metricsPublisher + "],"
                        + "propertiesStrategy [" + propertiesStrategy + "]," + "]");
                log.debug("Registering Sleuth Hystrix Concurrency Strategy.");
            }

            HystrixPlugins.reset();
            HystrixPlugins.getInstance().registerConcurrencyStrategy(target);
            HystrixPlugins.getInstance()
                    .registerCommandExecutionHook(commandExecutionHook);
            HystrixPlugins.getInstance().registerEventNotifier(eventNotifier);
            HystrixPlugins.getInstance().registerMetricsPublisher(metricsPublisher);
            HystrixPlugins.getInstance().registerPropertiesStrategy(propertiesStrategy);
        } catch (Exception e) {
            log.error("Failed to register Sleuth Hystrix Concurrency Strategy", e);
        }
    }

}
