package com.fisk.datagovernance.service.impl.datasecurity;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.datagovernance.entity.datasecurity.IntelligentDiscovery_WhiteListPO;
import com.fisk.datagovernance.mapper.datasecurity.IntelligentDiscovery_WhiteListMapper;
import com.fisk.datagovernance.service.datasecurity.IIntelligentDiscovery_WhiteListManageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class IntelligentDiscovery_WhiteListManageImpl extends ServiceImpl<IntelligentDiscovery_WhiteListMapper, IntelligentDiscovery_WhiteListPO> implements IIntelligentDiscovery_WhiteListManageService {
}
