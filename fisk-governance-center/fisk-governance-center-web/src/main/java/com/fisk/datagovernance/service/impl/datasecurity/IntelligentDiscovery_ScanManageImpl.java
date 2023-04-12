package com.fisk.datagovernance.service.impl.datasecurity;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.datagovernance.entity.datasecurity.IntelligentDiscovery_ScanPO;
import com.fisk.datagovernance.mapper.datasecurity.IntelligentDiscovery_ScanMapper;
import com.fisk.datagovernance.service.datasecurity.IIntelligentDiscovery_ScanManageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class IntelligentDiscovery_ScanManageImpl extends ServiceImpl<IntelligentDiscovery_ScanMapper, IntelligentDiscovery_ScanPO> implements IIntelligentDiscovery_ScanManageService {
}
