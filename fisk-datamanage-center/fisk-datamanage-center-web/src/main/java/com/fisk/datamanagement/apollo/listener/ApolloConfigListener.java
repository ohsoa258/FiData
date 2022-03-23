package com.fisk.datamanagement.apollo.listener;

import com.ctrip.framework.apollo.model.ConfigChange;
import com.ctrip.framework.apollo.model.ConfigChangeEvent;
import com.ctrip.framework.apollo.spring.annotation.ApolloConfigChangeListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.cloud.context.environment.EnvironmentChangeEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Configuration;

/**
 * Apollo 配置监听
 *
 * @author Lock
 */
@Configuration
public class ApolloConfigListener implements ApplicationContextAware {

    /**
     * 日志
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(ApolloConfigListener.class);

    private ApplicationContext applicationContext;

    /**
     * 配置监听
     * ApolloConfigChangeListener > value 属性默认 命名空间 "application"
     * <p>
     * 示例： @ApolloConfigChangeListener(value = {"application", "test_space"})
     */
    @ApolloConfigChangeListener(value = {"data-govern"})
    private void onChange(ConfigChangeEvent changeEvent) {
        LOGGER.warn("【Apollo-config-change】start");

        for (String key : changeEvent.changedKeys()) {
            ConfigChange change = changeEvent.getChange(key);
            LOGGER.warn("key={} , propertyName={} , oldValue={} , newValue={} ", key, change.getPropertyName(), change.getOldValue(), change.getNewValue());
        }
        // 更新相应的bean的属性值，主要是存在@ConfigurationProperties注解的bean
        this.applicationContext.publishEvent(new EnvironmentChangeEvent(changeEvent.changedKeys()));

        LOGGER.warn("【Apollo-config-change】end");
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}