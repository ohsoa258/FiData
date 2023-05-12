package com.fisk.task.service.pipeline.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.task.po.PipelineConfigurationPO;
import com.fisk.task.mapper.PipelineConfigurationMapper;
import com.fisk.task.service.pipeline.IPipelineConfiguration;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * @author cfk
 */
@Service
@Slf4j
public class PipelineConfigurationImpl extends ServiceImpl<PipelineConfigurationMapper, PipelineConfigurationPO> implements IPipelineConfiguration {
}
