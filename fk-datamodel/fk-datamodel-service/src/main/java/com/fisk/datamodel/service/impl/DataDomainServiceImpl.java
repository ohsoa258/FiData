package com.fisk.datamodel.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fisk.datamodel.dto.DataDomain.*;
import com.fisk.datamodel.entity.*;
import com.fisk.datamodel.mapper.*;
import com.fisk.datamodel.service.DataDomainService;
import com.fisk.datamodel.vo.DataDomainVO;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author WangYan
 * @date 2021/8/12 11:32
 */
@Service
@AllArgsConstructor
public class DataDomainServiceImpl implements DataDomainService {

    @Resource
    private BusinessAreaMapper businessMapper;

    @Resource
    private DimensionMapper dimensionMapper;

    @Resource
    private DimensionAttributeMapper dimensionAttributeMapper;

    @Resource
    private BusinessProcessMapper businessProcessMapper;

    @Resource
    private FactMapper factMapper;

    @Resource
    private IndicatorsMapper indicatorsMapper;

    @Override
    public Object getDataDomain() {
        List<BusinessAreaPO> businessAreaList = businessMapper.selectList(null);
        if (CollectionUtils.isEmpty(businessAreaList)) {
            return null;
        }

        DataDomainVO dataDomain = new DataDomainVO();
        List<AreaBusinessDTO> areaBusinessList = new ArrayList<>();
        for (BusinessAreaPO businessArea : businessAreaList) {
            AreaBusinessDTO area = new AreaBusinessDTO();
            area.setBusinessName(businessArea.getBusinessName());
            QueryWrapper<BusinessAreaPO> queryWrapper = new QueryWrapper<>();
            queryWrapper.lambda()
                    .eq(BusinessAreaPO::getBusinessName, businessArea.getBusinessName())
                    .select(BusinessAreaPO::getId);
            BusinessAreaPO areaBusiness = businessMapper.selectOne(queryWrapper);
            if (areaBusiness == null) {
                return null;
            }

            area.setBusinessId(areaBusiness.getId());
            // 查询二级维度
            List<DimensionPO> dimensionList = this.dimension(areaBusiness.getId());

            // 查询二级业务过程
            List<BusinessProcessPO> businessProcessList = this.businessProcess(areaBusiness.getId());

            // 查询三级维度字段表
            List<Long> longList = this.dimensionIds(dimensionList);

            // 查询业务过程ids
            List<Long> businessIds = businessIds(businessProcessList);

            // 拼接维度二级三级
            this.splicingDimensionList(dimensionList, longList, area);
            // 拼接业务过程二级,事实表三级,原子指标和派生指标四级
            this.splicingBusinessProcessList(businessProcessList, area, businessIds);

            areaBusinessList.add(area);
        }
        dataDomain.setAreaBusinessList(areaBusinessList);

        return dataDomain;
    }

    /**
     * 拼接维度二级,维度字段三级
     *
     * @param dimensionList dimensionList二级维度PO数据
     * @param longList      二级维度ids
     * @param area          拼接的对象
     */
    public void splicingDimensionList(List<DimensionPO> dimensionList, List<Long> longList, AreaBusinessDTO area) {
        if (CollectionUtils.isEmpty(dimensionList)) {
            return;
        }

        List<DimensionDTO> dimensionDTOList = new ArrayList<>();
        // 遍历二级维度查询出的数据
        for (DimensionPO dimension : dimensionList) {
            // 每一个二级维度
            DimensionDTO dto = new DimensionDTO();
            dto.setDimensionId(dimension.getId());
            dto.setDimensionCnName(dimension.getDimensionEnName());
            dto.setDimension(1);

            // 每一个二级对应多个三级
            List<DimensionAttributeDTO> dimensionAttributeDTOList = new ArrayList<>();
            List<DimensionAttributePO> dimensionAttributeList = this.dimensionField(dimension.getId());
            if (CollectionUtils.isEmpty(dimensionAttributeList)){
                return;
            }

            for (DimensionAttributePO dimensionAttribute : dimensionAttributeList) {
                DimensionAttributeDTO dimensionAttributeDTO = new DimensionAttributeDTO();
                dimensionAttributeDTO.setDimensionAttributeId(dimensionAttribute.getId());
                dimensionAttributeDTO.setDimensionFieldCnName(dimensionAttribute.getDimensionFieldEnName());
                dimensionAttributeDTO.setDimension(1);
                dimensionAttributeDTOList.add(dimensionAttributeDTO);
            }
            dto.setDimensionAttributeList(dimensionAttributeDTOList);


            dimensionDTOList.add(dto);
        }
        area.setDimensionList(dimensionDTOList);
    }

    /**
     * 拼接业务过程二级,原子指标和派生指标三级
     *
     * @param businessProcessList businessProcessList二级维度PO数据
     * @param area
     * @param businessIds
     */
    public void splicingBusinessProcessList(List<BusinessProcessPO> businessProcessList,
                                            AreaBusinessDTO area,
                                            List<Long> businessIds) {
        if(CollectionUtils.isEmpty(businessProcessList)){
            return;
        }

        List<BusinessProcessDTO> businessProcessDTOList = new ArrayList<>();
        for (BusinessProcessPO businessProcess : businessProcessList) {
            // 每一个二级业务过程
            BusinessProcessDTO dto = new BusinessProcessDTO();
            dto.setBusinessProcessId(businessProcess.getId());
            dto.setBusinessProcessCnName(businessProcess.getBusinessProcessEnName());
            dto.setDimension(0);

            List<FactDTO> factList = new ArrayList<>();
            if (CollectionUtils.isEmpty(businessIds)){
                return;
            }

            for (Long businessId : businessIds) {
                this.fact(businessId, factList);
            }

            dto.setFactList(factList);
            businessProcessDTOList.add(dto);
        }
        area.setBusinessProcessList(businessProcessDTOList);
    }

    /**
     * 拼接事实表FactDTO 三级
     *
     * @param businessId
     * @param factList
     */
    public void fact(Long businessId, List<FactDTO> factList) {
        if (businessId == 0 || businessId == null) {
            return;
        }

        QueryWrapper<FactPO> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda()
                .eq(FactPO::getBusinessProcessId, businessId)
                .select(FactPO::getId, FactPO::getFactTableEnName);
        List<FactPO> fact = factMapper.selectList(queryWrapper);
        if (CollectionUtils.isEmpty(fact)) {
            return;
        }

        for (FactPO factPO : fact) {
            List<AtomicIndicatorsDTO> atomicIndicatorsList = new ArrayList<>();
            List<DerivedIndicatorsDTO> derivedIndicatorsList = new ArrayList<>();
            // 拼接原子指标DTO 四级
            this.atomicIndicators(factPO.getId(), atomicIndicatorsList);
            // 拼接派生指标DTO 四级
            this.derivedIndicators(factPO.getId(), derivedIndicatorsList);

            FactDTO dto = new FactDTO();
            dto.setFactId(factPO.getId());
            dto.setFactTableEnName(factPO.getFactTableEnName());
            dto.setAtomicIndicatorsList(atomicIndicatorsList);
            dto.setDerivedIndicatorsList(derivedIndicatorsList);
            dto.setDimension(0);
            factList.add(dto);
        }
    }

    /**
     * 拼接原子指标DTO
     *
     * @param processId
     * @param atomicIndicatorsList
     */
    public void atomicIndicators(Long processId, List<AtomicIndicatorsDTO> atomicIndicatorsList) {
        if (processId == 0 || processId == null) {
            return;
        }

        QueryWrapper<IndicatorsPO> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda()
                .eq(IndicatorsPO::getFactId, processId)
                .eq(IndicatorsPO::getIndicatorsType, 0)
                .select(IndicatorsPO::getIndicatorsName, IndicatorsPO::getId);
        List<IndicatorsPO> atomicIndicators = indicatorsMapper.selectList(queryWrapper);
        if (CollectionUtils.isEmpty(atomicIndicators)) {
            return;
        }

        for (IndicatorsPO atomicIndicator : atomicIndicators) {
            AtomicIndicatorsDTO indicators = new AtomicIndicatorsDTO();
            indicators.setIndicatorsId(atomicIndicator.getId());
            indicators.setIndicatorsName(atomicIndicator.getIndicatorsName());
            indicators.setDimension(0);
            atomicIndicatorsList.add(indicators);
        }
    }

    /**
     * 拼接派生指标DTO
     *
     * @param processId
     * @param derivedIndicatorsList
     */
    public void derivedIndicators(Long processId, List<DerivedIndicatorsDTO> derivedIndicatorsList) {
        if (processId == 0 || processId == null) {
            return;
        }

        QueryWrapper<IndicatorsPO> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda()
                .eq(IndicatorsPO::getFactId, processId)
                .eq(IndicatorsPO::getIndicatorsType, 1);
        List<IndicatorsPO> derivedIndicators = indicatorsMapper.selectList(queryWrapper);
        if (CollectionUtils.isEmpty(derivedIndicators)) {
            return;
        }

        for (IndicatorsPO derivedIndicator : derivedIndicators) {
            DerivedIndicatorsDTO indicators = new DerivedIndicatorsDTO();
            indicators.setIndicatorsId(derivedIndicator.getId());
            indicators.setDerivedName(derivedIndicator.getIndicatorsName());
            indicators.setDimension(0);
            derivedIndicatorsList.add(indicators);
        }
    }

    /**
     * 查询二级维度
     *
     * @param businessId
     * @return
     */
    public List<DimensionPO> dimension(Long businessId) {
        if (businessId == 0 || businessId == null) {
            return null;
        }

        QueryWrapper<DimensionPO> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda()
                .eq(DimensionPO::getBusinessId, businessId)
                .select(DimensionPO::getDimensionEnName, DimensionPO::getId);
        List<DimensionPO> dimensionList = dimensionMapper.selectList(queryWrapper);
        if (CollectionUtils.isEmpty(dimensionList)) {
            return null;
        } else {
            return dimensionList;
        }
    }

    /**
     * 查询二级业务过程
     *
     * @param businessId
     */
    public List<BusinessProcessPO> businessProcess(Long businessId) {
        if (businessId == 0 || businessId == null) {
            return null;
        }

        QueryWrapper<BusinessProcessPO> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda()
                .eq(BusinessProcessPO::getBusinessId, businessId)
                .select(BusinessProcessPO::getBusinessProcessEnName, BusinessProcessPO::getId);

        List<BusinessProcessPO> businessProcessList = businessProcessMapper.selectList(queryWrapper);
        if (CollectionUtils.isEmpty(businessProcessList)) {
            return null;
        } else {
            return businessProcessList;
        }
    }

    /**
     * 查询二级维度id
     *
     * @param dimensionList
     * @return
     */
    public List<Long> dimensionIds(List<DimensionPO> dimensionList) {
        if (dimensionList == null) {
            return null;
        }

        List<Long> ids = new ArrayList<>();
        for (DimensionPO dimension : dimensionList) {
            ids.add(dimension.getId());
        }
        return ids;
    }

    /**
     * 查询业务过程id
     *
     * @param businessProcessList
     * @return
     */
    public List<Long> businessIds(List<BusinessProcessPO> businessProcessList) {
        if (CollectionUtils.isEmpty(businessProcessList)) {
            return null;
        }

        // 业务过程id
        List<Long> businessIds = new ArrayList<>();
        for (BusinessProcessPO businessProcess : businessProcessList) {
            businessIds.add(businessProcess.getId());
        }
        return businessIds;
    }

    /**
     * 查询三级维度字段
     *
     * @param dimensionId
     */
    public List<DimensionAttributePO> dimensionField(Long dimensionId) {
        if (dimensionId == 0 || dimensionId == null) {
            return null;
        }

        QueryWrapper<DimensionAttributePO> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda()
                .eq(DimensionAttributePO::getDimensionId, dimensionId)
                .select(DimensionAttributePO::getDimensionFieldEnName, DimensionAttributePO::getId);
        List<DimensionAttributePO> dimensionAttributeList = dimensionAttributeMapper.selectList(queryWrapper);
        if (CollectionUtils.isEmpty(dimensionAttributeList)) {
            return null;
        } else {
            return dimensionAttributeList;
        }
    }



    @Override
    public Object getDimension(){
        QueryWrapper<DimensionPO> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda()
                .select(DimensionPO::getId,DimensionPO::getDimensionCnName);

        List<DimensionPO> dimensionList = dimensionMapper.selectList(queryWrapper);
        if (CollectionUtils.isEmpty(dimensionList)){
            return null;
        }

        return dimensionList.stream()
                .map(e -> {
                    DimensionNameDTO dto = new DimensionNameDTO();
                    dto.setDimensionId(e.getId());
                    dto.setDimensionCnName(e.getDimensionCnName());
                    dto.setFlag(2);
                    return dto;
                }).collect(Collectors.toList());
    }



    @Override
    public List<AreaBusinessNameDTO> getBusiness(){
        List<BusinessAreaPO> businessAreaList = this.selectBusinessArea();
        if (CollectionUtils.isEmpty(businessAreaList)){
            return null;
        }

        return businessAreaList.stream()
                .map(e -> {
                    AreaBusinessNameDTO businessName = new AreaBusinessNameDTO();
                    businessName.setBusinessId(e.getId());
                    businessName.setBusinessName(e.getBusinessName());

                    List<BusinessProcessPO> businessProcessList = this.queryBusinessProcess(e.getId());
                    if (CollectionUtils.isEmpty(businessProcessList)){
                        return null;
                    }

                    businessName.setFlag(3);
                    List<BusinessProcessNameDTO> businessProcessNameDtoList = businessProcessList.stream()
                            .map(a -> {
                                BusinessProcessNameDTO businessProcessName = new BusinessProcessNameDTO();
                                businessProcessName.setBusinessProcessId(a.getId());
                                businessProcessName.setBusinessProcessCnName(a.getBusinessProcessCnName());

                                List<FactPO> factList = this.queryFact(a.getId());
                                if (CollectionUtils.isEmpty(factList)){
                                    return null;
                                }

                                businessProcessName.setFlag(4);
                                businessProcessName.setPid(e.getId());
                                List<FactNameDTO> factDtoList = factList.stream()
                                        .map(b -> {
                                            FactNameDTO factName = new FactNameDTO();
                                            factName.setFactId(b.getId());
                                            factName.setFactTableEnName(b.getFactTableEnName());
                                            factName.setFlag(5);
                                            factName.setPid(a.getId());
                                            return factName;
                                        }).collect(Collectors.toList());
                                businessProcessName.setFactList(factDtoList);
                                return businessProcessName;
                            }).collect(Collectors.toList());
                    businessName.setBusinessProcessList(businessProcessNameDtoList);
                    return businessName;
                }).collect(Collectors.toList());
    }

    /**
     * 根据业务域id查询业务过程
     * @param businessId 业务域Id
     * @return
     */
    public List<BusinessProcessPO> queryBusinessProcess(Long businessId){
        QueryWrapper<BusinessProcessPO> query = new QueryWrapper<>();
        query.lambda()
                .eq(BusinessProcessPO::getBusinessId,businessId)
                .select(BusinessProcessPO::getId,BusinessProcessPO::getBusinessProcessCnName);;
        List<BusinessProcessPO> businessProcessList = businessProcessMapper.selectList(query);
        if (CollectionUtils.isEmpty(businessProcessList)){
            return null;
        }else {
            return businessProcessList;
        }
    }

    /**
     * 根据业务过程id查询事实表
     * @param businessProcessId 业务过程id
     * @return
     */
    public List<FactPO> queryFact(Long businessProcessId){
        QueryWrapper<FactPO> query = new QueryWrapper<>();
        query.lambda()
                .eq(FactPO::getBusinessProcessId,businessProcessId)
                .select(FactPO::getId,FactPO::getFactTableEnName);
        List<FactPO> factList = factMapper.selectList(query);
        if (CollectionUtils.isEmpty(factList)){
            return null;
        }else {
            return factList;
        }
    }

    @Override
    public Object getAreaBusiness() {
        List<BusinessAreaPO> businessAreaList = this.selectBusinessArea();
        if (CollectionUtils.isEmpty(businessAreaList)){
            return null;
        }

        return businessAreaList.stream().map(e -> {
            BusinessDTO dto = new BusinessDTO();
            dto.setBusinessId(e.getId());
            dto.setBusinessName(e.getBusinessName());
            dto.setFlag(6);
            return dto;
        }).collect(Collectors.toList());
    }

    /**
     * 查询业务域
     * @return
     */
    public List<BusinessAreaPO> selectBusinessArea(){
        QueryWrapper<BusinessAreaPO> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda()
                .select(BusinessAreaPO::getId,BusinessAreaPO::getBusinessName);

        List<BusinessAreaPO> businessAreaList = businessMapper.selectList(queryWrapper);
        if (CollectionUtils.isEmpty(businessAreaList)) {
            return null;
        }

        return businessAreaList;
    }
}
