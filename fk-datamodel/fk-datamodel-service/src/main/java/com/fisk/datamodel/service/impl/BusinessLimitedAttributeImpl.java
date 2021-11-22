package com.fisk.datamodel.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fisk.common.exception.FkException;
import com.fisk.common.response.ResultEnum;
import com.fisk.common.user.UserHelper;
import com.fisk.datamodel.dto.businessLimited.BusinessLimitedAddDTO;
import com.fisk.datamodel.dto.businessLimited.BusinessLimitedDTO;
import com.fisk.datamodel.dto.businesslimitedattribute.BusinessLimitedAttributeAddDTO;
import com.fisk.datamodel.dto.businesslimitedattribute.BusinessLimitedAttributeDTO;
import com.fisk.datamodel.dto.businesslimitedattribute.BusinessLimitedAttributeDataDTO;
import com.fisk.datamodel.dto.factattribute.FactAttributeListDTO;
import com.fisk.datamodel.entity.BusinessLimitedAttributePO;
import com.fisk.datamodel.entity.BusinessLimitedPO;
import com.fisk.datamodel.map.BusinessLimitedAttributeMap;
import com.fisk.datamodel.map.BusinessLimitedMap;
import com.fisk.datamodel.mapper.BusinessLimitedAttributeMapper;
import com.fisk.datamodel.mapper.BusinessLimitedMapper;
import com.fisk.datamodel.mapper.FactAttributeMapper;
import com.fisk.datamodel.service.IBusinessLimitedAttribute;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author cfk
 */
@Service
public class BusinessLimitedAttributeImpl implements IBusinessLimitedAttribute {
    @Resource
    public BusinessLimitedMapper businessLimitedMapper;
    @Resource
    public BusinessLimitedAttributeMapper businessLimitedAttributeMapper;
    @Resource
    public FactAttributeMapper factAttributeMapper;

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
        BusinessLimitedAttributePO attributePO=businessLimitedAttributeMapper.selectOne(queryWrapper);
        if (attributePO !=null && attributePO.id !=dto.id)
        {
            return ResultEnum.DATA_EXISTS;
        }
        po.calculationLogic=dto.calculationLogic;
        po.calculationValue=dto.calculationValue;
        po.factAttributeId=dto.factAttributeId;
        return businessLimitedAttributeMapper.updateById(po)>0?ResultEnum.SUCCESS:ResultEnum.SAVE_DATA_ERROR;
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
