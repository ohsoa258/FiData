package com.fisk.datamanagement.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.datamanagement.entity.FacttreelistHistoryPO;
import com.fisk.datamanagement.mapper.FacttreelistHistoryMapper;
import com.fisk.datamanagement.service.FacttreelistHistoryService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service("facttreelistHistoryService")
public class FacttreelistHistoryServiceImpl extends ServiceImpl<FacttreelistHistoryMapper, FacttreelistHistoryPO> implements FacttreelistHistoryService {


    @Override
    public List<FacttreelistHistoryPO> selectHistoryId(String historyId) {
        return baseMapper.selectHistoryId(historyId);
    }
}
