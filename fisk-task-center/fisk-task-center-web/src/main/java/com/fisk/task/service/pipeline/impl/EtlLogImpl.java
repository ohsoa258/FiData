package com.fisk.task.service.pipeline.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.task.entity.TBETLlogPO;
import com.fisk.task.mapper.TBETLLogMapper;
import com.fisk.task.service.pipeline.IEtlLog;
import org.springframework.stereotype.Service;

@Service
public class EtlLogImpl extends ServiceImpl<TBETLLogMapper, TBETLlogPO> implements IEtlLog {
}
