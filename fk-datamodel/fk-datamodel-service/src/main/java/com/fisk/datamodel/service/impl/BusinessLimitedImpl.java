package com.fisk.datamodel.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fisk.common.response.ResultEnum;
import com.fisk.datamodel.dto.businessLimited.BusinessLimitedDTO;
import com.fisk.datamodel.dto.businessLimited.BusinessLimitedQueryDTO;
import com.fisk.datamodel.entity.BusinessLimitedAttributePO;
import com.fisk.datamodel.entity.BusinessLimitedPO;
import com.fisk.datamodel.mapper.BusinessLimitedAttributeMapper;
import com.fisk.datamodel.mapper.BusinessLimitedMapper;
import com.fisk.datamodel.service.IBusinessLimited;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author cfk
 */
@Service
public class BusinessLimitedImpl implements IBusinessLimited {
    @Resource
    public BusinessLimitedMapper businessLimitedMapper;
    @Resource
    public BusinessLimitedAttributeMapper businessLimitedAttributeMapper;
    @Override
    public Page<BusinessLimitedDTO> getBusinessLimitedDtoPage(BusinessLimitedQueryDTO businessLimitedDto) {
        Page<BusinessLimitedDTO> businessLimitedDtoPage = businessLimitedMapper.queryList(businessLimitedDto.page,businessLimitedDto);
        return businessLimitedDtoPage;
    }

    @Override
    public ResultEnum deleteBusinessLimitedById(String businessLimitedId) {
        QueryWrapper<BusinessLimitedAttributePO> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(BusinessLimitedAttributePO::getBusinessLimitedId,businessLimitedId);
        businessLimitedAttributeMapper.delete(queryWrapper);
        businessLimitedMapper.deleteById(businessLimitedId);
        return ResultEnum.SUCCESS;
    }

    @Override
    public List<BusinessLimitedPO> getBusinessLimitedList(String factId) {
        QueryWrapper<BusinessLimitedPO> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(BusinessLimitedPO::getFactId,factId);
        List<BusinessLimitedPO> businessLimitedPos = businessLimitedMapper.selectList(queryWrapper);
        return businessLimitedPos;
    }


}
