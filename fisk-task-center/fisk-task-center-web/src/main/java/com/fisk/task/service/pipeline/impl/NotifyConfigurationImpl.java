package com.fisk.task.service.pipeline.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.task.po.NotifyConfigurationPO;
import com.fisk.task.mapper.NotifyConfigurationMapper;
import com.fisk.task.service.pipeline.INotifyConfiguration;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * @author cfk
 */
@Service
@Slf4j
public class NotifyConfigurationImpl extends ServiceImpl<NotifyConfigurationMapper, NotifyConfigurationPO> implements INotifyConfiguration {
}
