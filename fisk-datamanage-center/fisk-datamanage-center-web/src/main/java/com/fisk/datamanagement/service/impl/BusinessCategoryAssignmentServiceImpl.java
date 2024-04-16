package com.fisk.datamanagement.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.datamanagement.entity.BusinessCategoryAssignmentPO;
import com.fisk.datamanagement.mapper.BusinessCategoryAssignmentMapper;
import com.fisk.datamanagement.service.BusinessCategoryAssignmentService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service("businessCategoryAssignmentService")
public class BusinessCategoryAssignmentServiceImpl extends ServiceImpl<BusinessCategoryAssignmentMapper, BusinessCategoryAssignmentPO> implements BusinessCategoryAssignmentService {


    @Override
    public List<Integer> getCategoryIds(List<Integer> roleIds) {
        return baseMapper.getCategoryIds(roleIds);
    }
}
