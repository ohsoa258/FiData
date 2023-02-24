package com.fisk.datamanagement.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.framework.exception.FkException;
import com.fisk.common.server.ocr.dto.businessmetadata.TableRuleInfoDTO;
import com.fisk.datamanagement.dto.metadatabusinessmetadatamap.EditMetadataBusinessMetadataMapDTO;
import com.fisk.datamanagement.dto.metadatabusinessmetadatamap.MetadataBusinessInfoDTO;
import com.fisk.datamanagement.dto.metadatabusinessmetadatamap.MetadataBusinessMetadataMapDTO;
import com.fisk.datamanagement.entity.BusinessMetadataConfigPO;
import com.fisk.datamanagement.entity.MetadataBusinessMetadataMapPO;
import com.fisk.datamanagement.map.MetadataBusinessMetadataMap;
import com.fisk.datamanagement.mapper.MetadataBusinessMetadataMapper;
import com.fisk.datamanagement.service.IMetadataBusinessMetadataMap;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author JianWenYang
 */
@Service
public class MetadataBusinessMetadataMapImpl
        extends ServiceImpl<MetadataBusinessMetadataMapper, MetadataBusinessMetadataMapPO>
        implements IMetadataBusinessMetadataMap {

    @Resource
    MetadataBusinessMetadataMapper mapper;

    @Transactional(rollbackFor = Exception.class)
    public ResultEnum operationMetadataBusinessMetadataMap(EditMetadataBusinessMetadataMapDTO dtoList) {

        delMetadataBusinessMetadataMap(dtoList.metadataEntityId);

        addMetadataBusinessMetadataMap(dtoList.list);

        return ResultEnum.SUCCESS;
    }

    @Override
    public ResultEnum addMetadataBusinessMetadataMap(List<MetadataBusinessMetadataMapDTO> dtoList) {
        if (CollectionUtils.isEmpty(dtoList)) {
            return ResultEnum.SUCCESS;
        }
        List<MetadataBusinessMetadataMapPO> poList = MetadataBusinessMetadataMap.INSTANCES.dtoListToPoList(dtoList);

        boolean flat = this.saveBatch(poList);
        if (!flat) {
            throw new FkException(ResultEnum.SAVE_DATA_ERROR);
        }

        return ResultEnum.SUCCESS;
    }

    @Override
    public ResultEnum delMetadataBusinessMetadataMap(Integer metadataEntityId) {

        QueryWrapper<MetadataBusinessMetadataMapPO> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(MetadataBusinessMetadataMapPO::getMetadataEntityId, metadataEntityId);

        List<MetadataBusinessMetadataMapPO> poList = mapper.selectList(queryWrapper);
        if (CollectionUtils.isEmpty(poList)) {
            return ResultEnum.SUCCESS;
        }

        boolean flat = this.remove(queryWrapper);
        if (!flat) {
            throw new FkException(ResultEnum.SAVE_DATA_ERROR);
        }

        return ResultEnum.SUCCESS;
    }

    /**
     * 获取业务元数据信息
     *
     * @param metadataEntityId
     * @return
     */
    public Map getBusinessMetadata(String metadataEntityId) {

        Map map = new HashMap<>();
        //查询数据
        List<MetadataBusinessInfoDTO> list = mapper.selectMetadataBusiness(Integer.parseInt(metadataEntityId));
        //根据业务元数据类别分组
        Map<String, List<MetadataBusinessInfoDTO>> data = list.stream()
                .collect(Collectors.groupingBy(MetadataBusinessInfoDTO::getBusinessMetadataName));
        if (CollectionUtils.isEmpty(data)) {
            return map;
        }

        for (String item : data.keySet()) {
            List<MetadataBusinessInfoDTO> metadataBusinessInfoDTOS = data.get(item);
            if (CollectionUtils.isEmpty(metadataBusinessInfoDTOS)) {
                continue;
            }
            //根据业务元数据名称分组
            Map<String, List<MetadataBusinessInfoDTO>> collect = metadataBusinessInfoDTOS
                    .stream().collect(Collectors.groupingBy(MetadataBusinessInfoDTO::getAttributeCnName));
            if (CollectionUtils.isEmpty(collect)) {
                continue;
            }

            Map map1 = new HashMap();
            for (String name : collect.keySet()) {
                List<MetadataBusinessInfoDTO> attribute = collect.get(name);
                map1.put(name, attribute.stream().map(e -> e.value).collect(Collectors.toList()));
            }

            map.put(item, map1);
        }


        return map;
    }

    /**
     * 设置质量规则
     *
     * @param data
     * @param metadataEntityId
     * @param tableRuleInfoDTO
     * @return
     */
    public List<MetadataBusinessMetadataMapDTO> setQualityRules(List<BusinessMetadataConfigPO> data,
                                                                Integer metadataEntityId,
                                                                TableRuleInfoDTO tableRuleInfoDTO) {
        List<MetadataBusinessMetadataMapDTO> list = new ArrayList<>();
        //校验规则
        for (BusinessMetadataConfigPO item : data) {
            switch (item.attributeName) {
                case "ValidationRules":
                    list.addAll(setBusinessMetadata(metadataEntityId, item.id, tableRuleInfoDTO.checkRules));
                    break;
                case "CleaningRules":
                    list.addAll(setBusinessMetadata(metadataEntityId, item.id, tableRuleInfoDTO.filterRules));
                    break;
                case "LifeCycle":
                    list.addAll(setBusinessMetadata(metadataEntityId, item.id, tableRuleInfoDTO.lifecycleRules));
                    break;
                case "AlarmSet":
                    list.addAll(setBusinessMetadata(metadataEntityId, item.id, tableRuleInfoDTO.noticeRules));
                    break;
                default:
                    break;
            }
        }
        return list;
    }

    /**
     * 设置业务定义
     *
     * @param data
     * @param metadataEntityId
     * @param tableRuleInfoDTO
     * @return
     */
    public List<MetadataBusinessMetadataMapDTO> setBusinessDefinition(List<BusinessMetadataConfigPO> data,
                                                                      Integer metadataEntityId,
                                                                      TableRuleInfoDTO tableRuleInfoDTO) {
        List<MetadataBusinessMetadataMapDTO> list = new ArrayList<>();
        for (BusinessMetadataConfigPO item : data) {
            if ("BusinessName".equals(item.attributeName)) {
                list.addAll(setBusinessMetadata(metadataEntityId, item.id, tableRuleInfoDTO.businessName));
            }
        }
        return list;
    }

    /**
     * 设置业务规则
     *
     * @param data
     * @param metadataEntityId
     * @param tableRuleInfoDTO
     * @return
     */
    public List<MetadataBusinessMetadataMapDTO> setBusinessRules(List<BusinessMetadataConfigPO> data,
                                                                 Integer metadataEntityId,
                                                                 TableRuleInfoDTO tableRuleInfoDTO) {
        List<MetadataBusinessMetadataMapDTO> list = new ArrayList<>();

        for (BusinessMetadataConfigPO item : data) {
            switch (item.attributeName) {
                case "UpdateRules":
                    list.addAll(setBusinessMetadata(metadataEntityId, item.id, tableRuleInfoDTO.updateRules));
                    break;
                case "TransformationRules":
                    list.addAll(setBusinessMetadata(metadataEntityId, item.id, tableRuleInfoDTO.transformationRules));
                    break;
                case "ComputationalFormula":
                    list.addAll(setBusinessMetadata(metadataEntityId, item.id, ""));
                    break;
                case "KnownDataProblem":
                    list.addAll(setBusinessMetadata(metadataEntityId, item.id, tableRuleInfoDTO.knownDataProblem));
                    break;
                case "DirectionsForUse":
                    list.addAll(setBusinessMetadata(metadataEntityId, item.id, tableRuleInfoDTO.directionsForUse));
                    break;
                case "ValidValueConstraint":
                    list.addAll(setBusinessMetadata(metadataEntityId, item.id, tableRuleInfoDTO.validValueConstraint));
                    break;
                default:
                    break;
            }
        }

        return list;
    }

    /**
     * 设置管理规则
     *
     * @param data
     * @param metadataEntityId
     * @param tableRuleInfoDTO
     * @return
     */
    public List<MetadataBusinessMetadataMapDTO> setManagementRules(List<BusinessMetadataConfigPO> data,
                                                                   Integer metadataEntityId,
                                                                   TableRuleInfoDTO tableRuleInfoDTO) {
        List<MetadataBusinessMetadataMapDTO> list = new ArrayList<>();
        for (BusinessMetadataConfigPO item : data) {
            switch (item.attributeName) {
                case "DataResponsibilityDepartment":
                    list.addAll(setBusinessMetadata(metadataEntityId, item.id, ""));
                    break;
                case "DataResponsiblePerson":
                    list.addAll(setBusinessMetadata(metadataEntityId, item.id, tableRuleInfoDTO.dataResponsiblePerson));
                    break;
                case "Stakeholders":
                    list.addAll(setBusinessMetadata(metadataEntityId, item.id, tableRuleInfoDTO.stakeholders));
                    break;
                default:
                    break;
            }
        }

        return list;
    }

    /**
     * 业务元数据设置
     *
     * @param metadataEntityId
     * @param businessMetadataId
     * @param value
     * @return
     */
    public List<MetadataBusinessMetadataMapDTO> setBusinessMetadata(Integer metadataEntityId,
                                                                    long businessMetadataId,
                                                                    String value) {
        List<MetadataBusinessMetadataMapDTO> list = new ArrayList<>();
        MetadataBusinessMetadataMapDTO dto = new MetadataBusinessMetadataMapDTO();
        dto.metadataEntityId = metadataEntityId;
        dto.businessMetadataId = (int) businessMetadataId;
        dto.value = value;
        list.add(dto);
        return list;
    }

    /**
     * 业务元数据设置
     *
     * @param metadataEntityId
     * @param businessMetadataId
     * @param value
     * @return
     */
    public List<MetadataBusinessMetadataMapDTO> setBusinessMetadata(Integer metadataEntityId,
                                                                    long businessMetadataId,
                                                                    List<String> value) {
        List<MetadataBusinessMetadataMapDTO> list = new ArrayList<>();
        for (String item : value) {
            MetadataBusinessMetadataMapDTO dto = new MetadataBusinessMetadataMapDTO();
            dto.metadataEntityId = metadataEntityId;
            dto.businessMetadataId = (int) businessMetadataId;
            dto.value = item;

            list.add(dto);
        }
        return list;
    }

}
