package com.fisk.task.service.pipeline.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.task.dto.task.NifiSchedulingComponentPO;
import com.fisk.task.mapper.NifiSchedulingComponentMapper;
import com.fisk.task.service.pipeline.INifiSchedulingComponentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class NifiSchedulingComponentImpl extends ServiceImpl<NifiSchedulingComponentMapper, NifiSchedulingComponentPO> implements INifiSchedulingComponentService {
}
