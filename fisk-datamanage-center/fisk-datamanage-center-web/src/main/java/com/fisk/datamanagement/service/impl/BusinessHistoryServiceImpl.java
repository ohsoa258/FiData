package com.fisk.datamanagement.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.datamanagement.entity.BusinessHistoryPO;
import com.fisk.datamanagement.mapper.BusinessHistoryMapper;
import com.fisk.datamanagement.service.BusinessHistoryService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service("businessHistoryService")
public class BusinessHistoryServiceImpl extends ServiceImpl<BusinessHistoryMapper, BusinessHistoryPO> implements BusinessHistoryService {


    @Override
    public List<String> getHistoryId(Integer id) {
        return baseMapper.getHistoryId(id);
    }
}
