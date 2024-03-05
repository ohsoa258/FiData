package com.fisk.datamanagement.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.datamanagement.entity.BusinessTargetinfoHistoryPO;
import com.fisk.datamanagement.mapper.BusinessTargetinfoHistoryMapper;
import com.fisk.datamanagement.service.BusinessTargetinfoHistoryService;
import org.springframework.stereotype.Service;

@Service("businessTargetinfoHistoryService")
public class BusinessTargetinfoHistoryServiceImpl extends ServiceImpl<BusinessTargetinfoHistoryMapper, BusinessTargetinfoHistoryPO> implements BusinessTargetinfoHistoryService {


    @Override
    public BusinessTargetinfoHistoryPO selectClassification(String historyId) {
        return baseMapper.selectClassification(historyId);
    }
}
