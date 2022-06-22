package com.fisk.datamanagement.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.datamanagement.dto.assetsdirectory.AssetsDirectoryDTO;
import com.fisk.datamanagement.entity.MetadataMapAtlasPO;
import com.fisk.datamanagement.enums.EntityTypeEnum;
import com.fisk.datamanagement.enums.TableTypeEnum;
import com.fisk.datamanagement.mapper.MetadataMapAtlasMapper;
import com.fisk.datamanagement.service.IAssetsDirectory;
import com.fisk.datamodel.client.DataModelClient;
import com.fisk.datamodel.dto.tableconfig.SourceFieldDTO;
import com.fisk.datamodel.dto.tableconfig.SourceTableDTO;
import com.fisk.datamodel.enums.DataModelTableTypeEnum;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * @author JianWenYang
 */
@Service
public class AssetsDirectoryImpl implements IAssetsDirectory {

    @Resource
    DataModelClient client;

    @Resource
    MetadataMapAtlasMapper metadataMapAtlasMapper;

    @Override
    public List<AssetsDirectoryDTO> assetsDirectoryData() {
        List<AssetsDirectoryDTO> data = new ArrayList<>();
        String firstLevel = UUID.randomUUID().toString();
        data.add(setAssetsDirectory(firstLevel, "资产目录", ""));
        data.addAll(getAnalyzeDataList(firstLevel));
        return data;
    }

    /**
     * 资产目录-分析数据--维度、业务过程
     *
     * @return
     */
    public List<AssetsDirectoryDTO> getAnalyzeDataList(String firstLevel) {
        List<AssetsDirectoryDTO> data = new ArrayList<>();
        String analyzeDataKey = UUID.randomUUID().toString();
        data.add(setAssetsDirectory(analyzeDataKey, "分析数据", firstLevel));
        //维度key
        String dimensionKey = UUID.randomUUID().toString();
        //业务过程key
        String businessProcessKey = UUID.randomUUID().toString();
        data.add(setAssetsDirectory(dimensionKey, "维度", analyzeDataKey));
        data.add(setAssetsDirectory(businessProcessKey, "业务过程", analyzeDataKey));
        List<Integer> type = new ArrayList<>();
        type.add(TableTypeEnum.DW_DIMENSION.getValue());
        type.add(TableTypeEnum.DW_FACT.getValue());
        //读取dw中的维度和事实
        QueryWrapper<MetadataMapAtlasPO> queryWrapper = new QueryWrapper<>();
        queryWrapper.in("table_type", type.toArray()).lambda()
                .eq(MetadataMapAtlasPO::getType, EntityTypeEnum.RDBMS_TABLE);
        List<MetadataMapAtlasPO> list = metadataMapAtlasMapper.selectList(queryWrapper);
        //调用数仓建模模块接口,获取表名
        ResultEntity<Object> result = client.getDataModelTable(1);
        if (result.code != ResultEnum.SUCCESS.getCode() || CollectionUtils.isEmpty(list)) {
            return data;
        }
        List<SourceTableDTO> sourceTableList = JSON.parseArray(JSON.toJSONString(result.data), SourceTableDTO.class);
        //循环赋值
        for (MetadataMapAtlasPO item : list) {
            Optional<SourceTableDTO> first = sourceTableList.stream()
                    .filter(e -> e.id == item.tableId && e.type == item.tableType)
                    .findFirst();
            if (!first.isPresent()) {
                continue;
            }
            //维度
            if (item.tableType == TableTypeEnum.DW_DIMENSION.getValue()) {
                data.add(setAssetsDirectory(item.atlasGuid, first.get().tableName, dimensionKey));
                continue;
            }
            //业务过程
            data.add(setAssetsDirectory(item.atlasGuid, first.get().tableName, businessProcessKey));
        }
        data.addAll(getAnalysisModel(analyzeDataKey));
        return data;
    }

    /**
     * 资产目录-分析模型--原子指标、派生指标、宽表
     *
     * @return
     */
    public List<AssetsDirectoryDTO> getAnalysisModel(String analyzeDataKey) {
        List<AssetsDirectoryDTO> data = new ArrayList<>();
        //分析模型key
        String analysisModelKey = UUID.randomUUID().toString();
        data.add(setAssetsDirectory(analysisModelKey, "分析模型", analyzeDataKey));
        //原子指标key
        String atomicIndicatorsKey = UUID.randomUUID().toString();
        data.add(setAssetsDirectory(atomicIndicatorsKey, "原子指标", analysisModelKey));
        //派生指标key
        String derivedIndicatorsKey = UUID.randomUUID().toString();
        data.add(setAssetsDirectory(derivedIndicatorsKey, "派生指标", analysisModelKey));
        //宽表key
        String wideTableKey = UUID.randomUUID().toString();
        data.add(setAssetsDirectory(wideTableKey, "宽表", analysisModelKey));
        List<Integer> type = new ArrayList<>();
        type.add(TableTypeEnum.DW_FACT.getValue());
        type.add(TableTypeEnum.DORIS_DIMENSION.getValue());
        //查询属性为原子指标和派生指标的数据
        QueryWrapper<MetadataMapAtlasPO> queryWrapper = new QueryWrapper<>();
        queryWrapper.in("attribute_type", type);
        List<MetadataMapAtlasPO> list = metadataMapAtlasMapper.selectList(queryWrapper);
        //调用数仓建模模块接口,获取表名
        ResultEntity<Object> result = client.getDataModelTable(2);
        if (result.code != ResultEnum.SUCCESS.getCode() || CollectionUtils.isEmpty(list)) {
            return data;
        }
        List<SourceTableDTO> sourceTableList = JSON.parseArray(JSON.toJSONString(result.data), SourceTableDTO.class);
        for (MetadataMapAtlasPO item : list) {
            Optional<SourceTableDTO> first = sourceTableList
                    .stream()
                    .filter(e -> e.type == TableTypeEnum.DORIS_FACT.getValue() && e.id == item.tableId)
                    .findFirst();
            if (!first.isPresent()) {
                continue;
            }
            Optional<SourceFieldDTO> atomic = first.get().fieldList.stream().filter(e -> e.id == item.columnId).findFirst();
            if (!atomic.isPresent()) {
                continue;
            }
            //原子指标
            if (atomic.get().attributeType == 2) {
                data.add(setAssetsDirectory(item.atlasGuid, atomic.get().fieldName, atomicIndicatorsKey));
                continue;
            }
            data.add(setAssetsDirectory(item.atlasGuid, atomic.get().fieldName, derivedIndicatorsKey));
        }
        data.addAll(getWideTableList(wideTableKey));
        return data;
    }

    public List<AssetsDirectoryDTO> getWideTableList(String wideTableKey) {
        List<AssetsDirectoryDTO> data = new ArrayList<>();
        //调用宽表接口,获取表名
        ResultEntity<Object> result = client.getDataModelTable(3);
        if (result.code != ResultEnum.SUCCESS.getCode()) {
            return data;
        }
        List<SourceTableDTO> sourceTableList = JSON.parseArray(JSON.toJSONString(result.data), SourceTableDTO.class);
        List<SourceTableDTO> collect = sourceTableList.stream().filter(e -> e.type == DataModelTableTypeEnum.WIDE_TABLE.getValue()).collect(Collectors.toList());
        //宽表
        QueryWrapper<MetadataMapAtlasPO> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(MetadataMapAtlasPO::getTableType, DataModelTableTypeEnum.WIDE_TABLE.getValue());
        List<MetadataMapAtlasPO> poList = metadataMapAtlasMapper.selectList(queryWrapper);
        for (MetadataMapAtlasPO item : poList) {
            Optional<SourceTableDTO> first = collect.stream().filter(e -> e.id == item.id).findFirst();
            if (!first.isPresent()) {
                continue;
            }
            data.add(setAssetsDirectory(item.atlasGuid, first.get().tableName, wideTableKey));
        }
        return data;

    }

    public AssetsDirectoryDTO setAssetsDirectory(String key, String name, String parent) {
        AssetsDirectoryDTO dto = new AssetsDirectoryDTO();
        dto.key = key;
        dto.name = name;
        dto.parent = parent;
        return dto;
    }

}
