package com.fisk.datagovernance.service.impl.datasecurity;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.datagovernance.entity.datasecurity.IntelligentDiscovery_UserPO;
import com.fisk.datagovernance.mapper.datasecurity.IntelligentDiscovery_UserMapper;
import com.fisk.datagovernance.service.datasecurity.IIntelligentDiscovery_UserManageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class IntelligentDiscovery_UserManageImpl extends ServiceImpl<IntelligentDiscovery_UserMapper, IntelligentDiscovery_UserPO> implements IIntelligentDiscovery_UserManageService {
}
