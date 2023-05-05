package com.fisk.task.service.pipeline.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.task.po.app.NifiConfigPO;
import com.fisk.task.mapper.NifiConfigMapper;
import com.fisk.task.service.pipeline.INifiConfigService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * @author cfk
 */
@Service
@Slf4j
public class NifiConfigServiceImpl extends ServiceImpl<NifiConfigMapper, NifiConfigPO> implements INifiConfigService {
}
