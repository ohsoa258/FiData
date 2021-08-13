package com.fisk.dataservice.service.impl;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fisk.common.exception.FkException;
import com.fisk.common.response.ResultEnum;
import com.fisk.dataservice.dto.BusinessProcessDTO;
import com.fisk.dataservice.dto.DimensionAttributeDTO;
import com.fisk.dataservice.dto.DimensionDTO;
import com.fisk.dataservice.entity.AreaBusinessPO;
import com.fisk.dataservice.entity.DimensionAttributePO;
import com.fisk.dataservice.entity.DimensionPO;
import com.fisk.dataservice.mapper.AreaBusinessMapper;
import com.fisk.dataservice.mapper.DimensionAttributeMapper;
import com.fisk.dataservice.mapper.DimensionMapper;
import com.fisk.dataservice.service.DataDomainService;
import com.fisk.dataservice.vo.DataDomainVO;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * @author WangYan
 * @date 2021/8/12 11:32
 */
@Service
@AllArgsConstructor
public class DataDomainServiceImpl implements DataDomainService {

    @Resource
    private AreaBusinessMapper businessMapper;

    @Resource
    private DimensionMapper dimensionMapper;

    @Resource
    private DimensionAttributeMapper dimensionAttributeMapper;

    @DS("datamodel")
    @Override
    public Object getDataDomain(String businessName) {
        DataDomainVO dataDomain = new DataDomainVO();
        dataDomain.setBusinessName(businessName);
        QueryWrapper<AreaBusinessPO> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda()
                .eq(AreaBusinessPO::getBusinessName, businessName)
                .select(AreaBusinessPO::getId);
        AreaBusinessPO areaBusiness = businessMapper.selectOne(queryWrapper);
        if (areaBusiness == null) {
            throw new FkException(ResultEnum.DATA_NOTEXISTS);
        }

        // 查询二级维度
        List<DimensionDTO> dimensionDTOList = new ArrayList<>();
        List<DimensionPO> dimensionList = this.dimension(areaBusiness.getId());
        //List<BusinessProcessDTO> businessProcessList = this;

        // 查询三级维度字段表
        List<Long> longList = this.dimensionIds(dimensionList);

        // 遍历二级维度查询出的数据
        for (DimensionPO dimension : dimensionList) {
            // 每一个二级维度
            DimensionDTO dto = new DimensionDTO();
            dto.setDimensionCnName( dimension.getDimensionCnName());

            for (Long aLong : longList) {
                // 每一个二级对应多个三级
                List<DimensionAttributeDTO> dimensionAttributeDTOList = new ArrayList<>();
                List<DimensionAttributePO> dimensionAttributeList = this.dimensionField(aLong);
                for (DimensionAttributePO dimensionAttribute : dimensionAttributeList) {
                    DimensionAttributeDTO dimensionAttributeDTO = new DimensionAttributeDTO();
                    dimensionAttributeDTO.setDimensionFieldCnName(dimensionAttribute.getDimensionFieldCnName());
                    dimensionAttributeDTOList.add(dimensionAttributeDTO);
                }
                dto.setDimensionAttributeList(dimensionAttributeDTOList);
            }

            dimensionDTOList.add(dto);
        }
        dataDomain.setDimensionList(dimensionDTOList);

        return dataDomain;
    }

    /**
     * 查询二级维度
     *
     * @param businessId
     * @return
     */
    public List<DimensionPO> dimension(Long businessId) {
        QueryWrapper<DimensionPO> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda()
                .eq(DimensionPO::getBusinessId, businessId)
                .select(DimensionPO::getDimensionCnName,DimensionPO::getId);
        List<DimensionPO> dimensionList = dimensionMapper.selectList(queryWrapper);
        if (CollectionUtils.isEmpty(dimensionList)) {
            return null;
        } else {
            return dimensionList;
        }
    }

    /**
     * 查询二级维度id
     *
     * @param dimensionList
     * @return
     */
    public List<Long> dimensionIds(List<DimensionPO> dimensionList) {
        List<Long> ids = new ArrayList<>();
        for (DimensionPO dimension : dimensionList) {
            ids.add(dimension.getId());
        }
        return ids;
    }

    /**
     * 查询三级维度字段
     *
     * @param dimensionId
     */
    public List<DimensionAttributePO> dimensionField(Long dimensionId) {
        QueryWrapper<DimensionAttributePO> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda()
                .eq(DimensionAttributePO::getDimensionId, dimensionId)
                .select(DimensionAttributePO::getDimensionFieldCnName);
        List<DimensionAttributePO> dimensionAttributeList = dimensionAttributeMapper.selectList(queryWrapper);
        if (CollectionUtils.isEmpty(dimensionAttributeList)) {
            return null;
        } else {
            return dimensionAttributeList;
        }
    }
}
