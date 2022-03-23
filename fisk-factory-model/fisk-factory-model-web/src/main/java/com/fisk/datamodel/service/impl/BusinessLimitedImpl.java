package com.fisk.datamodel.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fisk.common.exception.FkException;
import com.fisk.common.response.ResultEnum;
import com.fisk.datamodel.dto.businessLimited.*;
import com.fisk.datamodel.entity.BusinessLimitedAttributePO;
import com.fisk.datamodel.entity.BusinessLimitedPO;
import com.fisk.datamodel.map.BusinessLimitedAttributeMap;
import com.fisk.datamodel.map.BusinessLimitedMap;
import com.fisk.datamodel.map.BusinessProcessMap;
import com.fisk.datamodel.mapper.BusinessLimitedAttributeMapper;
import com.fisk.datamodel.mapper.BusinessLimitedMapper;
import com.fisk.datamodel.service.IBusinessLimited;
import com.sun.org.apache.regexp.internal.RE;
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

    /*代码分段*/


    @Override
    public List<BusinessLimitedPO> getBusinessLimitedList(String factId) {
        QueryWrapper<BusinessLimitedPO> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(BusinessLimitedPO::getFactId,factId);
        List<BusinessLimitedPO> businessLimitedPos = businessLimitedMapper.selectList(queryWrapper);
        return businessLimitedPos;
    }

    @Override
    public BusinessLimitedDataDTO getBusinessLimitedAndAttributeList(int businessLimitedId)
    {
        BusinessLimitedPO po=businessLimitedMapper.selectById(businessLimitedId);
        if (po==null)
        {
            throw new FkException(ResultEnum.DATA_NOTEXISTS);
        }
        BusinessLimitedDataDTO dto=new BusinessLimitedDataDTO();
        dto.id=po.id;
        dto.limitedName=po.limitedName;
        dto.limitedDes=po.limitedDes;
        //获取业务限定字段列表
        QueryWrapper<BusinessLimitedAttributePO> queryWrapper=new QueryWrapper<>();
        queryWrapper.lambda().eq(BusinessLimitedAttributePO::getBusinessLimitedId,businessLimitedId);
        List<BusinessLimitedAttributePO> list=businessLimitedAttributeMapper.selectList(queryWrapper);
        if (list==null || list.size()==0)
        {
            return  dto;
        }
        dto.dto= BusinessLimitedAttributeMap.INSTANCES.poListToDto(list);
        return dto;
    }

    @Override
    public ResultEnum BusinessLimitedUpdate(BusinessLimitedUpdateDTO dto)
    {
        BusinessLimitedPO po=businessLimitedMapper.selectById(dto.id);
        if (po==null)
        {
            throw new FkException(ResultEnum.DATA_NOTEXISTS);
        }
        po.limitedDes=dto.limitedDes;
        po.limitedName=dto.limitedName;
        return businessLimitedMapper.updateById(po)>0?ResultEnum.SUCCESS:ResultEnum.SAVE_DATA_ERROR;
    }

    @Override
    public ResultEnum BusinessLimitedAdd(BusinessLimitedDataAddDTO dto)
    {
        QueryWrapper<BusinessLimitedPO> queryWrapper=new QueryWrapper<>();
        queryWrapper.lambda().eq(BusinessLimitedPO::getLimitedName,dto.limitedName)
                .eq(BusinessLimitedPO::getFactId,dto.id);
        BusinessLimitedPO po=businessLimitedMapper.selectOne(queryWrapper);
        if (po !=null)
        {
            return ResultEnum.DATA_EXISTS;
        }
        return businessLimitedMapper.insert(BusinessLimitedMap.INSTANCES.dtoAddToPo(dto))>0?ResultEnum.SUCCESS:ResultEnum.DATA_NOTEXISTS;
    }


}
