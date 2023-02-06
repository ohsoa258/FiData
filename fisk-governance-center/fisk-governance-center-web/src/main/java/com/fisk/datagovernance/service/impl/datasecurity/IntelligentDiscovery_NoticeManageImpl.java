package com.fisk.datagovernance.service.impl.datasecurity;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.datagovernance.entity.datasecurity.IntelligentDiscovery_NoticePO;
import com.fisk.datagovernance.mapper.datasecurity.IntelligentDiscovery_NoticeMapper;
import com.fisk.datagovernance.service.datasecurity.IIntelligentDiscovery_NoticeManageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class IntelligentDiscovery_NoticeManageImpl extends ServiceImpl<IntelligentDiscovery_NoticeMapper, IntelligentDiscovery_NoticePO> implements IIntelligentDiscovery_NoticeManageService {
}
