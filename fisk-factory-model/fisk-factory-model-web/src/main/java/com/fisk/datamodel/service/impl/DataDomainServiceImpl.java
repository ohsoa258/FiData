package com.fisk.datamodel.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.fisk.datamodel.dto.datadomain.*;
import com.fisk.datamodel.entity.*;
import com.fisk.datamodel.mapper.*;
import com.fisk.datamodel.service.DataDomainService;
import com.fisk.chartvisual.vo.DataDomainVO;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.fisk.chartvisual.enums.NodeTypeEnum.*;

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
    public List<DataDomainVO> getDataDomain() {
        List<BusinessAreaPO> businessAreaList = businessMapper.selectList(null);
        if (CollectionUtils.isEmpty(businessAreaList)) {
            return null;
        }

        return businessAreaList.stream().filter(Objects::nonNull)
                .map(e -> {
                    DataDomainVO dataDomain = new DataDomainVO();
                    dataDomain.setId(e.getId());
                    dataDomain.setName(e.getBusinessName());
                    dataDomain.setDimensionType(BUSINESS_DOMAIN);
                    dataDomain.setChildren(this.dataDomainMerge(this.spliceDimension(e.getId()),this.splicingBusinessProcess(e.getId())));

                    return dataDomain;
                }).collect(Collectors.toList());
    }

    /**
     * 二级维度和业务过程合并
     * @param dimensionList
     * @param businessProcessList
     * @return
     */
    public List<DataDomainVO> dataDomainMerge(List<DataDomainVO> dimensionList,List<DataDomainVO> businessProcessList){
        List<DataDomainVO> domainList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(dimensionList)){
            domainList.addAll(dimensionList);
        }

        if (CollectionUtils.isNotEmpty(businessProcessList)){
            domainList.addAll(businessProcessList);
        }
        return domainList;
    }

    /**
     * 四级原子和派生指标合并
     * @param factId
     * @return
     */
    public List<DataDomainVO> indicatorMerge(Long factId){
        List<DataDomainVO> dataDomainList = new ArrayList<>();
        List<IndicatorsPO> indicatorsList = this.atomicIndicators(factId);
        if (CollectionUtils.isNotEmpty(indicatorsList)) {
                    // 四级原子
            List<DataDomainVO> atomList = indicatorsList.stream().filter(Objects::nonNull)
                    .map(iter -> new DataDomainVO(iter.getId(), iter.getIndicatorsName(), ATOMIC_METRICS))
                    .collect(Collectors.toList());
            dataDomainList.addAll(atomList);
        }

        List<IndicatorsPO> derivedList = this.derivedIndicators(factId);
        if (CollectionUtils.isNotEmpty(derivedList)) {
                    // 四级派生
            List<DataDomainVO> deriveList = derivedList.stream().filter(Objects::nonNull)
                    .map(iter -> new DataDomainVO(iter.getId(), iter.getIndicatorsName(), DERIVED_METRICS))
                    .collect(Collectors.toList());
            dataDomainList.addAll(deriveList);
        }

        return dataDomainList;
    }

    /**
     * 拼接二级维度三级维度字段
     * @param businessId
     * @return
     */
    public List<DataDomainVO> spliceDimension(Long businessId){
        List<DimensionPO> dimensionList = this.dimension(businessId);
        if (CollectionUtils.isNotEmpty(dimensionList)) {
            // 二级维度和三级维度字段
            // 维度
            return dimensionList.stream().filter(Objects::nonNull)
                    .map(item -> {
                        DataDomainVO dataDomain1 = new DataDomainVO();
                        dataDomain1.setId(item.getId());
                        dataDomain1.setName(item.getDimensionTabName());
                        dataDomain1.setDimensionType(OTHER);

                        List<DimensionAttributePO> dimensionAttributeList = this.dimensionField(item.getId());
                        if (CollectionUtils.isNotEmpty(dimensionAttributeList)) {
                            dataDomain1.setChildren(
                                    // 维度字段
                                    dimensionAttributeList.stream().filter(Objects::nonNull)
                                            .map(it -> new DataDomainVO(it.getId(), it.getDimensionFieldEnName(), DIMENSION_FIELD))
                                            .collect(Collectors.toList())
                            );
                        }

                        return dataDomain1;
                    }).collect(Collectors.toList());
        }

        return null;
    }

    /**
     * 拼接业务过程二级,事实表三级,原子指标和派生指标四级
     * @param businessId
     * @return
     */
    public List<DataDomainVO> splicingBusinessProcess(Long businessId){
        List<BusinessProcessPO> processList = this.businessProcess(businessId);
        if (CollectionUtils.isNotEmpty(processList)) {
            // 二级业务过程和三级事实 四级原子和派生
            // 二级业务过程
            return processList.stream().filter(Objects::nonNull)
                    .map(item -> {
                        DataDomainVO dataDomain1 = new DataDomainVO();
                        dataDomain1.setId(item.getId());
                        dataDomain1.setName(item.getBusinessProcessEnName());
                        dataDomain1.setDimensionType(BUSINESS_PROCESS);

                        List<FactPO> factList = this.fact(item.getId());
                        if (CollectionUtils.isNotEmpty(factList)) {
                            dataDomain1.setChildren(
                                    // 三级事实
                                    factList.stream().filter(Objects::nonNull)
                                            .map(it -> {
                                                DataDomainVO dataDomain2 = new DataDomainVO();
                                                dataDomain2.setId(it.getId());
                                                dataDomain2.setName(it.getFactTabName());
                                                dataDomain2.setDimensionType(FACT);
                                                dataDomain2.setChildren(this.indicatorMerge(it.getId()));
                                                return dataDomain2;
                                            }).collect(Collectors.toList()));
                        }


                        return dataDomain1;
                    }).collect(Collectors.toList());
        }

        return null;
    }

    /**
     * 根据业务域id查询事实表
     * @param businessId
     * @return
     */
    public List<FactPO> fact(Long businessId) {
        if (businessId == 0 || businessId == null) {
            return null;
        }

        QueryWrapper<FactPO> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda()
                .eq(FactPO::getBusinessProcessId, businessId)
                .select(FactPO::getId, FactPO::getFactTabName);
        List<FactPO> fact = factMapper.selectList(queryWrapper);
        if (CollectionUtils.isEmpty(fact)) {
            return null;
        }

        return fact;
    }

    /**
     * 根据事实表id查询原子指标
     * @param processId
     * @return
     */
    public List<IndicatorsPO> atomicIndicators(Long processId) {
        if (processId == 0 || processId == null) {
            return null;
        }

        QueryWrapper<IndicatorsPO> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda()
                .eq(IndicatorsPO::getFactId, processId)
                .eq(IndicatorsPO::getIndicatorsType, 0)
                .select(IndicatorsPO::getIndicatorsName, IndicatorsPO::getId);
        List<IndicatorsPO> atomicIndicators = indicatorsMapper.selectList(queryWrapper);
        if (CollectionUtils.isEmpty(atomicIndicators)) {
            return null;
        }

        return atomicIndicators;
    }

    /**
     * 根据事实表id查询派生指标
     * @param processId
     * @return
     */
    public List<IndicatorsPO> derivedIndicators(Long processId) {
        if (processId == 0 || processId == null) {
            return null;
        }

        QueryWrapper<IndicatorsPO> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda()
                .eq(IndicatorsPO::getFactId, processId)
                .eq(IndicatorsPO::getIndicatorsType, 1);
        List<IndicatorsPO> derivedIndicators = indicatorsMapper.selectList(queryWrapper);
        if (CollectionUtils.isEmpty(derivedIndicators)) {
            return null;
        }

        return derivedIndicators;
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
                .select(DimensionPO::getDimensionTabName, DimensionPO::getId);
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
    public Object getDimension() {
        List<BusinessAreaPO> businessAreaList = this.selectBusinessArea();
        if (CollectionUtils.isEmpty(businessAreaList)) {
            return null;
        }

        List<AreaBusinessDimDTO> areaBusinessDTOList = businessAreaList.stream()
                .map(e -> {
                    AreaBusinessDimDTO dto = new AreaBusinessDimDTO();
                    dto.setBusinessId(e.getId());
                    dto.setBusinessName(e.getBusinessName());

                    List<DimensionPO> dimensionList = this.getDimension(e.getId());
                    if (CollectionUtils.isEmpty(dimensionList)) {
                        return null;
                    }

                    // 二级 维度
                    List<DimensionDimDTO> dimensionDtoList = dimensionList.stream()
                            .map(a -> {
                                DimensionDimDTO dimension = new DimensionDimDTO();
                                dimension.setDimensionId(a.getId());
                                dimension.setDimensionCnName(a.getDimensionCnName());
                                dimension.setFlag(8);
                                dimension.setPid(e.getId());
                                return dimension;
                            }).collect(Collectors.toList());

                    List<BusinessProcessPO> businessProcessList = this.queryBusinessProcess(e.getId());
                    if (CollectionUtils.isEmpty(businessProcessList)) {
                        return null;
                    }

                    // 二级 业务过程
                    List<BusinessProcessDimDTO> businessProcessDtoList = businessProcessList.stream()
                            .map(b -> {
                                BusinessProcessDimDTO businessProcess = new BusinessProcessDimDTO();
                                businessProcess.setBusinessProcessId(b.getId());
                                businessProcess.setBusinessProcessCnName(b.getBusinessProcessCnName());

                                List<FactPO> factList = this.queryFact(b.getId());
                                if (CollectionUtils.isEmpty(factList)) {
                                    return null;
                                }

                                List<FactDimDTO> factDtoList = factList.stream()
                                        .map(c -> {
                                            FactDimDTO fact = new FactDimDTO();
                                            fact.setFactId(c.getId());
                                            fact.setFactTableEnName(c.getFactTableEnName());
                                            fact.setFlag(10);
                                            fact.setPid(b.getId());
                                            return fact;
                                        }).collect(Collectors.toList());
                                businessProcess.setFlag(9);
                                businessProcess.setPid(e.getId());
                                businessProcess.setFactList(factDtoList);
                                return businessProcess;
                            }).collect(Collectors.toList());

                    dto.setFlag(7);
                    dto.setDimensionList(dimensionDtoList);
                    dto.setBusinessProcessList(businessProcessDtoList);
                    return dto;
                }).collect(Collectors.toList());

        return areaBusinessDTOList.stream().filter(Objects::nonNull).collect(Collectors.toList());
    }

    /**
     * 根据业务域id查询维度表
     *
     * @param businessId
     * @return
     */
    public List<DimensionPO> getDimension(Long businessId) {
        QueryWrapper<DimensionPO> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda()
                .eq(DimensionPO::getBusinessId, businessId)
                .select(DimensionPO::getId, DimensionPO::getDimensionCnName);

        List<DimensionPO> dimensionList = dimensionMapper.selectList(queryWrapper);
        if (CollectionUtils.isEmpty(dimensionList)) {
            return null;
        } else {
            return dimensionList;
        }
    }

    @Override
    public List<AreaBusinessNameDTO> getBusiness() {
        List<BusinessAreaPO> businessAreaList = this.selectBusinessArea();
        if (CollectionUtils.isEmpty(businessAreaList)) {
            return null;
        }

        List<AreaBusinessNameDTO> dtoList = businessAreaList.stream()
                .map(e -> {
                    AreaBusinessNameDTO businessName = new AreaBusinessNameDTO();
                    businessName.setBusinessId(e.getId());
                    businessName.setBusinessName(e.getBusinessName());

                    List<BusinessProcessPO> businessProcessList = this.queryBusinessProcess(e.getId());
                    if (CollectionUtils.isEmpty(businessProcessList)) {
                        return null;
                    }

                    businessName.setFlag(3);
                    List<BusinessProcessNameDTO> businessProcessNameDtoList = businessProcessList.stream()
                            .map(a -> {
                                BusinessProcessNameDTO businessProcessName = new BusinessProcessNameDTO();
                                businessProcessName.setBusinessProcessId(a.getId());
                                businessProcessName.setBusinessProcessCnName(a.getBusinessProcessCnName());

                                List<FactPO> factList = this.queryFact(a.getId());
                                if (CollectionUtils.isEmpty(factList)) {
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

        return dtoList.stream().filter(Objects::nonNull).collect(Collectors.toList());
    }

    /**
     * 根据业务域id查询业务过程
     *
     * @param businessId 业务域Id
     * @return
     */
    public List<BusinessProcessPO> queryBusinessProcess(Long businessId) {
        QueryWrapper<BusinessProcessPO> query = new QueryWrapper<>();
        query.lambda()
                .eq(BusinessProcessPO::getBusinessId, businessId)
                .select(BusinessProcessPO::getId, BusinessProcessPO::getBusinessProcessCnName);
        List<BusinessProcessPO> businessProcessList = businessProcessMapper.selectList(query);
        if (CollectionUtils.isEmpty(businessProcessList)) {
            return null;
        } else {
            return businessProcessList;
        }
    }

    /**
     * 根据业务过程id查询事实表
     *
     * @param businessProcessId 业务过程id
     * @return
     */
    public List<FactPO> queryFact(Long businessProcessId) {
        QueryWrapper<FactPO> query = new QueryWrapper<>();
        query.lambda()
                .eq(FactPO::getBusinessProcessId, businessProcessId)
                .select(FactPO::getId, FactPO::getFactTableEnName);
        List<FactPO> factList = factMapper.selectList(query);
        if (CollectionUtils.isEmpty(factList)) {
            return null;
        } else {
            return factList;
        }
    }

    @Override
    public Object getAreaBusiness() {
        List<BusinessAreaPO> businessAreaList = this.selectBusinessArea();
        if (CollectionUtils.isEmpty(businessAreaList)) {
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
     *
     * @return
     */
    public List<BusinessAreaPO> selectBusinessArea() {
        QueryWrapper<BusinessAreaPO> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda()
                .select(BusinessAreaPO::getId, BusinessAreaPO::getBusinessName);

        List<BusinessAreaPO> businessAreaList = businessMapper.selectList(queryWrapper);
        if (CollectionUtils.isEmpty(businessAreaList)) {
            return null;
        }

        return businessAreaList;
    }
}
