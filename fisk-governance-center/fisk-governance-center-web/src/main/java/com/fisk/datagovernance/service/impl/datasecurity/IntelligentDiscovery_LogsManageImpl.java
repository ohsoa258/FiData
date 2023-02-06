package com.fisk.datagovernance.service.impl.datasecurity;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.datagovernance.entity.datasecurity.IntelligentDiscovery_LogsPO;
import com.fisk.datagovernance.mapper.datasecurity.IntelligentDiscovery_LogsMapper;
import com.fisk.datagovernance.service.datasecurity.IIntelligentDiscovery_LogsManageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class IntelligentDiscovery_LogsManageImpl extends ServiceImpl<IntelligentDiscovery_LogsMapper, IntelligentDiscovery_LogsPO> implements IIntelligentDiscovery_LogsManageService {
}
