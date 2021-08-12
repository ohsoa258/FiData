package com.fisk.datamodel.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fisk.common.response.ResultEnum;
import com.fisk.common.user.UserHelper;
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
    @Resource
    public UserHelper userHelper;

    @Override
    public ResultEnum updateBusinessLimitedAttribute(BusinessLimitedAddDTO businessLimitedAddDTO) {
        Long id = userHelper.getLoginUserInfo().id;
        if (businessLimitedAddDTO.id != 0) {
            BusinessLimitedDTO businessLimitedDto = new BusinessLimitedDTO();
            businessLimitedAddDTO.updateTime=new Date();
            businessLimitedAddDTO.updateUser=id;
            BusinessLimitedDtoToBusinessLimitedAddDto(businessLimitedDto,businessLimitedAddDTO);
            businessLimitedMapper.updateById(BusinessLimitedMap.INSTANCES.dtoTopo(businessLimitedDto));
        } else {
            businessLimitedAddDTO.createTime=new Date();
            businessLimitedAddDTO.createUser=id;
            businessLimitedMapper.insertBusinessLimited(businessLimitedAddDTO);
        }
        QueryWrapper<BusinessLimitedAttributePO> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(BusinessLimitedAttributePO::getBusinessLimitedId,businessLimitedAddDTO.id);
        businessLimitedAttributeMapper.delete(queryWrapper);
        BusinessLimitedAttributeDTO businessLimitedAttributeDto = new BusinessLimitedAttributeDTO();
        List<BusinessLimitedAttributeAddDTO> businessLimitedAttributeAddDTOList = businessLimitedAddDTO.businessLimitedAttributeAddDTOList;
        for (BusinessLimitedAttributeAddDTO businessLimitedAttributeAddDto : businessLimitedAttributeAddDTOList) {
            /*//状态:0原有,1新增,2修改,3删除
            if (businessLimitedAttributeAddDTO.funcType == 0) {
                //原有的不变
            } else if (businessLimitedAttributeAddDTO.funcType == 1) {
                businessLimitedAttributeMapper.insert(BusinessLimitedAttributeMap.INSTANCES.dtoTopo(businessLimitedAttributeAddDTO));
            } else if (businessLimitedAttributeAddDTO.funcType == 2) {
                businessLimitedAttributeMapper.updateById(BusinessLimitedAttributeMap.INSTANCES.dtoTopo(businessLimitedAttributeAddDTO));
            } else if (businessLimitedAttributeAddDTO.funcType == 3) {
                businessLimitedAttributeMapper.deleteByIdWithFill(BusinessLimitedAttributeMap.INSTANCES.dtoTopo(businessLimitedAttributeAddDTO));
            }*/

            businessLimitedAttributeAddDto.businessLimitedId=businessLimitedAddDTO.id;
            businessLimitedAttributeAddDto.createTime=new Date();
            businessLimitedAttributeAddDto.createUser=id;
            BusinessLimitedAttributeDtoToBusinessLimitedAttributeAddDto(businessLimitedAttributeDto,businessLimitedAttributeAddDto);
            businessLimitedAttributeMapper.insert(BusinessLimitedAttributeMap.INSTANCES.dtoTopo(businessLimitedAttributeDto));
        }
        return ResultEnum.SUCCESS;
    }

    @Override
    public BusinessLimitedAddDTO getBusinessLimitedAttribute(String businessLimitedId) {
        BusinessLimitedPO businessLimitedPo = businessLimitedMapper.selectById(businessLimitedId);
        BusinessLimitedDTO businessLimitedDto = BusinessLimitedMap.INSTANCES.poToDto(businessLimitedPo);
        BusinessLimitedAddDTO businessLimitedAddDto = new BusinessLimitedAddDTO();
        BusinessLimitedDtoToBusinessLimitedAddDto(businessLimitedDto, businessLimitedAddDto);
        QueryWrapper<BusinessLimitedAttributePO> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(BusinessLimitedAttributePO::getBusinessLimitedId, businessLimitedId);
        List<BusinessLimitedAttributePO> businessLimitedAttributePos = businessLimitedAttributeMapper.selectList(queryWrapper);
        ArrayList<BusinessLimitedAttributeAddDTO> businessLimitedAttributeDtos = new ArrayList<>();
        BusinessLimitedAttributeAddDTO businessLimitedAttributeAddDto = new BusinessLimitedAttributeAddDTO();
        for (BusinessLimitedAttributePO businessLimitedAttributePo : businessLimitedAttributePos) {
            BusinessLimitedAttributeDTO businessLimitedAttributeDTO = BusinessLimitedAttributeMap.INSTANCES.poTodto(businessLimitedAttributePo);
            BusinessLimitedAttributeDtoToBusinessLimitedAttributeAddDto(businessLimitedAttributeDTO, businessLimitedAttributeAddDto);
            businessLimitedAttributeDtos.add(businessLimitedAttributeAddDto);
        }
        //赋值业务限定字段条件
        businessLimitedAddDto.businessLimitedAttributeAddDTOList = businessLimitedAttributeDtos;
        //获取下拉框事实表字段
        List<FactAttributeListDTO> factAttributeList = factAttributeMapper.getFactAttributeList(businessLimitedPo.factId);
        businessLimitedAddDto.factAttributeListDtoList=factAttributeList;
        return businessLimitedAddDto;
    }

    private void BusinessLimitedDtoToBusinessLimitedAddDto(BusinessLimitedDTO businessLimitedDto, BusinessLimitedAddDTO businessLimitedAddDto) {
        businessLimitedDto.id = businessLimitedAddDto.id;
        businessLimitedDto.limitedDes = businessLimitedAddDto.limitedDes;
        businessLimitedDto.limitedName = businessLimitedAddDto.limitedName;
        businessLimitedDto.createTime = businessLimitedAddDto.createTime;
        businessLimitedDto.createUser = businessLimitedAddDto.createUser;
        businessLimitedDto.updateTime = businessLimitedAddDto.updateTime;
        businessLimitedDto.updateUser = businessLimitedAddDto.updateUser;
        businessLimitedDto.factId = businessLimitedAddDto.factId;
    }

    private void BusinessLimitedAttributeDtoToBusinessLimitedAttributeAddDto(BusinessLimitedAttributeDTO businessLimitedAttributeDto, BusinessLimitedAttributeAddDTO businessLimitedAttributeAddDto) {
        businessLimitedAttributeDto.id = businessLimitedAttributeAddDto.id;
        businessLimitedAttributeDto.businessLimitedId = businessLimitedAttributeAddDto.businessLimitedId;
        businessLimitedAttributeDto.factAttributeId = businessLimitedAttributeAddDto.factAttributeId;
        businessLimitedAttributeDto.calculationLogic = businessLimitedAttributeAddDto.calculationLogic;
        businessLimitedAttributeDto.calculationValue = businessLimitedAttributeAddDto.calculationValue;
        businessLimitedAttributeDto.createTime = businessLimitedAttributeAddDto.createTime;
        businessLimitedAttributeDto.createUser = businessLimitedAttributeAddDto.createUser;
        businessLimitedAttributeDto.updateTime = businessLimitedAttributeAddDto.updateTime;
        businessLimitedAttributeDto.updateUser = businessLimitedAttributeAddDto.updateUser;
    }
}
