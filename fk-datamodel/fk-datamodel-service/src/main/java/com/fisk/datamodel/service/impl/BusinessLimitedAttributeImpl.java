package com.fisk.datamodel.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fisk.common.response.ResultEnum;
import com.fisk.datamodel.dto.businessLimited.BusinessLimitedAddDTO;
import com.fisk.datamodel.dto.businessLimited.BusinessLimitedDTO;
import com.fisk.datamodel.dto.businesslimitedattribute.BusinessLimitedAttributeAddDTO;
import com.fisk.datamodel.dto.businesslimitedattribute.BusinessLimitedAttributeDTO;
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
    public ResultEnum updateBusinessLimitedAttribute(BusinessLimitedAddDTO businessLimitedAddDTO) {
        if (businessLimitedAddDTO.id != 0) {
            businessLimitedMapper.updateById(BusinessLimitedMap.INSTANCES.dtoTopo(businessLimitedAddDTO));
        } else {
            businessLimitedMapper.insert(BusinessLimitedMap.INSTANCES.dtoTopo(businessLimitedAddDTO));
        }
        List<BusinessLimitedAttributeAddDTO> businessLimitedAttributeAddDTOList = businessLimitedAddDTO.businessLimitedAttributeAddDTOList;
        for (BusinessLimitedAttributeAddDTO businessLimitedAttributeAddDTO : businessLimitedAttributeAddDTOList
        ) {
            //状态:0原有,1新增,2修改,3删除
            if (businessLimitedAttributeAddDTO.funcType == 0) {
                //原有的不变
            } else if (businessLimitedAttributeAddDTO.funcType == 1) {
                businessLimitedAttributeMapper.insert(BusinessLimitedAttributeMap.INSTANCES.dtoTopo(businessLimitedAttributeAddDTO));
            } else if (businessLimitedAttributeAddDTO.funcType == 2) {
                businessLimitedAttributeMapper.updateById(BusinessLimitedAttributeMap.INSTANCES.dtoTopo(businessLimitedAttributeAddDTO));
            } else if (businessLimitedAttributeAddDTO.funcType == 3) {
                businessLimitedAttributeMapper.deleteByIdWithFill(BusinessLimitedAttributeMap.INSTANCES.dtoTopo(businessLimitedAttributeAddDTO));
            }
        }
        return ResultEnum.SUCCESS;
    }


    //获取一个
    @Override
    public BusinessLimitedAddDTO getBusinessLimitedAttribute(String businessLimitedId) {
        BusinessLimitedPO businessLimitedPO = businessLimitedMapper.selectById(businessLimitedId);
        BusinessLimitedDTO businessLimitedDTO1 = BusinessLimitedMap.INSTANCES.poToDto(businessLimitedPO);
        BusinessLimitedAddDTO businessLimitedDTO = new BusinessLimitedAddDTO();
        BusinessLimitedDTOToBusinessLimitedAddDTO(businessLimitedDTO1, businessLimitedDTO);
        QueryWrapper<BusinessLimitedAttributePO> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(BusinessLimitedAttributePO::getBusinessLimitedId, businessLimitedId);
        List<BusinessLimitedAttributePO> businessLimitedAttributePOS = businessLimitedAttributeMapper.selectList(queryWrapper);
        ArrayList<BusinessLimitedAttributeAddDTO> businessLimitedAttributeDTOS = new ArrayList<>();
        BusinessLimitedAttributeAddDTO businessLimitedAttributeAddDTO = new BusinessLimitedAttributeAddDTO();
        for (BusinessLimitedAttributePO businessLimitedAttributePO : businessLimitedAttributePOS) {
            BusinessLimitedAttributeDTO businessLimitedAttributeDTO = BusinessLimitedAttributeMap.INSTANCES.poTodto(businessLimitedAttributePO);
            BusinessLimitedAttributeDTOToBusinessLimitedAttributeAddDTO(businessLimitedAttributeDTO, businessLimitedAttributeAddDTO);
            businessLimitedAttributeDTOS.add(businessLimitedAttributeAddDTO);
        }
        //赋值业务限定字段条件
        businessLimitedDTO.businessLimitedAttributeAddDTOList = businessLimitedAttributeDTOS;
        //获取下拉框事实表字段
        List<FactAttributeListDTO> factAttributeList = factAttributeMapper.getFactAttributeList(businessLimitedPO.factId);
        businessLimitedDTO.factAttributeListDTOList=factAttributeList;
        return businessLimitedDTO;
    }
    //类型转换
    private void BusinessLimitedDTOToBusinessLimitedAddDTO(BusinessLimitedDTO businessLimitedDTO1, BusinessLimitedAddDTO businessLimitedDTO) {
        businessLimitedDTO.id = businessLimitedDTO1.id;
        businessLimitedDTO.limitedDes = businessLimitedDTO1.limitedDes;
        businessLimitedDTO.limitedName = businessLimitedDTO1.limitedName;
    }
    //类型转换
    private void BusinessLimitedAttributeDTOToBusinessLimitedAttributeAddDTO(BusinessLimitedAttributeDTO businessLimitedAttributeDTO, BusinessLimitedAttributeAddDTO businessLimitedAttributeAddDTO) {
        businessLimitedAttributeAddDTO.id = businessLimitedAttributeDTO.id;
        businessLimitedAttributeAddDTO.businessLimitedId = businessLimitedAttributeDTO.businessLimitedId;
        businessLimitedAttributeAddDTO.factAttributeId = businessLimitedAttributeDTO.factAttributeId;
        businessLimitedAttributeAddDTO.calculationLogic = businessLimitedAttributeDTO.calculationLogic;
        businessLimitedAttributeAddDTO.calculationValue = businessLimitedAttributeDTO.calculationValue;
    }
}
