package com.fisk.datamanagement.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.datamanagement.entity.BusinessExtendedfieldsHistoryPO;
import com.fisk.datamanagement.mapper.BusinessExtendedfieldsHistoryMapper;
import com.fisk.datamanagement.service.BusinessExtendedfieldsHistoryService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service("businessExtendedfieldsHistoryService")
public class BusinessExtendedfieldsHistoryServiceImpl extends ServiceImpl<BusinessExtendedfieldsHistoryMapper, BusinessExtendedfieldsHistoryPO> implements BusinessExtendedfieldsHistoryService {


    @Override
    public List<BusinessExtendedfieldsHistoryPO> selectHistoryId(String historyId) {
        return baseMapper.selectHistoryId(historyId);
    }
}
