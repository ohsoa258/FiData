package com.fisk.datamanagement.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.datamanagement.entity.ApitreelistHistoryPO;
import com.fisk.datamanagement.mapper.ApitreelistHistoryMapper;
import com.fisk.datamanagement.service.ApitreelistHistoryService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service("apitreelistHistoryService")
public class ApitreelistHistoryServiceImpl extends ServiceImpl<ApitreelistHistoryMapper, ApitreelistHistoryPO> implements ApitreelistHistoryService {


    @Override
    public List<ApitreelistHistoryPO> selectHistoryId(String historyId) {
        return baseMapper.selectHistoryId(historyId);
    }
}
