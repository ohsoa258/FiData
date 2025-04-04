package com.fisk.datamodel.service.impl.businesslimited;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.framework.exception.FkException;
import com.fisk.datamodel.dto.businesslimitedattribute.BusinessLimitedAttributeDataDTO;
import com.fisk.datamodel.entity.businesslimited.BusinessLimitedAttributePO;
import com.fisk.datamodel.map.businesslimited.BusinessLimitedAttributeMap;
import com.fisk.datamodel.mapper.businesslimited.BusinessLimitedAttributeMapper;
import com.fisk.datamodel.service.IBusinessLimitedAttribute;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @author cfk
 */
@Service
public class BusinessLimitedAttributeImpl implements IBusinessLimitedAttribute {
    @Resource
    public BusinessLimitedAttributeMapper businessLimitedAttributeMapper;

    @Override
    public ResultEnum updateBusinessLimitedAttribute(BusinessLimitedAttributeDataDTO dto)
    {
        BusinessLimitedAttributePO po=businessLimitedAttributeMapper.selectById(dto.id);
        if (po==null)
        {
            throw new FkException(ResultEnum.DATA_NOTEXISTS);
        }
        //判断是否重复
        QueryWrapper<BusinessLimitedAttributePO> queryWrapper=new QueryWrapper<>();
        queryWrapper.lambda()
                .eq(BusinessLimitedAttributePO::getBusinessLimitedId,dto.businessLimitedId)
                .eq(BusinessLimitedAttributePO::getCalculationLogic,dto.calculationLogic)
                .eq(BusinessLimitedAttributePO::getCalculationValue,dto.calculationValue)
                .eq(BusinessLimitedAttributePO::getFactAttributeId,dto.factAttributeId);
        BusinessLimitedAttributePO attributePo=businessLimitedAttributeMapper.selectOne(queryWrapper);
        if (attributePo !=null && attributePo.id !=dto.id)
        {
            return ResultEnum.DATA_EXISTS;
        }
        return businessLimitedAttributeMapper.updateById(BusinessLimitedAttributeMap.INSTANCES.dtoToPo(dto))>0?ResultEnum.SUCCESS:ResultEnum.SAVE_DATA_ERROR;
    }

    @Override
    public ResultEnum delBusinessLimitedAttribute(int id)
    {
        BusinessLimitedAttributePO po=businessLimitedAttributeMapper.selectById(id);
        if (po==null)
        {
            throw new FkException(ResultEnum.DATA_NOTEXISTS);
        }
        return businessLimitedAttributeMapper.deleteByIdWithFill(po)>0?ResultEnum.SUCCESS:ResultEnum.SAVE_DATA_ERROR;
    }

    @Override
    public ResultEnum addBusinessLimitedAttribute(BusinessLimitedAttributeDataDTO dto)
    {
        //判断是否重复
        QueryWrapper<BusinessLimitedAttributePO> queryWrapper=new QueryWrapper<>();
        queryWrapper.lambda()
                .eq(BusinessLimitedAttributePO::getBusinessLimitedId,dto.businessLimitedId)
                .eq(BusinessLimitedAttributePO::getCalculationLogic,dto.calculationLogic)
                .eq(BusinessLimitedAttributePO::getCalculationValue,dto.calculationValue)
                .eq(BusinessLimitedAttributePO::getFactAttributeId,dto.factAttributeId);
        BusinessLimitedAttributePO po=businessLimitedAttributeMapper.selectOne(queryWrapper);
        if (po !=null)
        {
            return ResultEnum.DATA_EXISTS;
        }
        return businessLimitedAttributeMapper.insert(BusinessLimitedAttributeMap.INSTANCES.dtoToPo(dto))>0?ResultEnum.SUCCESS:ResultEnum.SAVE_DATA_ERROR;
    }



}
