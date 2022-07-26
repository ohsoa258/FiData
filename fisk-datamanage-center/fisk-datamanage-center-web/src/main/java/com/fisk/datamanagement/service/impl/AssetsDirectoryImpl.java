package com.fisk.datamanagement.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fisk.common.core.enums.fidatadatasource.DataSourceConfigEnum;
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
import com.fisk.datamodel.enums.FactAttributeEnum;
import com.fisk.system.client.UserClient;
import com.fisk.system.dto.datasource.DataSourceDTO;
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
    UserClient userClient;

    @Resource
    MetadataMapAtlasMapper metadataMapAtlasMapper;

    @Override
    public List<AssetsDirectoryDTO> assetsDirectoryData() {
        List<AssetsDirectoryDTO> data = new ArrayList<>();
        String firstLevel = UUID.randomUUID().toString();
        //第一级目录
        data.add(setAssetsDirectory(firstLevel, "资产目录", "", 1, false));
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
        data.add(setAssetsDirectory(analyzeDataKey, "分析数据", firstLevel, 2, false));
        //维度key
        String dimensionKey = UUID.randomUUID().toString();
        data.add(setAssetsDirectory(dimensionKey, "维度", analyzeDataKey, 3, false));
        //业务过程key
        String businessProcessKey = UUID.randomUUID().toString();
        data.add(setAssetsDirectory(businessProcessKey, "业务过程", analyzeDataKey, 3, false));

        //数据库限定名
        String dbQualifiedName = getDataSource(DataSourceConfigEnum.DMP_DW.getValue());
        //读取dw中的维度和事实
        QueryWrapper<MetadataMapAtlasPO> queryWrapper = new QueryWrapper<>();
        queryWrapper
                .lambda()
                .eq(MetadataMapAtlasPO::getType, EntityTypeEnum.RDBMS_TABLE);
        List<MetadataMapAtlasPO> list = metadataMapAtlasMapper.selectList(queryWrapper);
        //调用数仓建模模块接口,获取表名
        ResultEntity<Object> result = client.getDataModelTable(1);
        if (result.code != ResultEnum.SUCCESS.getCode() || CollectionUtils.isEmpty(list)) {
            return data;
        }
        List<SourceTableDTO> sourceTableList = JSON.parseArray(JSON.toJSONString(result.data), SourceTableDTO.class);
        //循环赋值
        for (SourceTableDTO item : sourceTableList) {
            //维度
            if (item.type == TableTypeEnum.DW_DIMENSION.getValue()) {
                String tableQualifiedName = dbQualifiedName + "_" + DataModelTableTypeEnum.DW_DIMENSION.getValue() + "_" + item.id;
                Optional<MetadataMapAtlasPO> first = list.stream().filter(e -> tableQualifiedName.equals(e.qualifiedName)).findFirst();
                if (!first.isPresent()) {
                    continue;
                }
                data.add(setAssetsDirectory(first.get().atlasGuid, item.tableName, dimensionKey, 4, true));
                continue;
            }
            //业务过程
            String tableQualifiedName = dbQualifiedName + "_" + DataModelTableTypeEnum.DW_FACT.getValue() + "_" + item.id;
            Optional<MetadataMapAtlasPO> first = list.stream().filter(e -> tableQualifiedName.equals(e.qualifiedName)).findFirst();
            if (!first.isPresent()) {
                continue;
            }
            data.add(setAssetsDirectory(first.get().atlasGuid, item.tableName, dimensionKey, 4, true));
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
        data.add(setAssetsDirectory(analysisModelKey, "分析模型", analyzeDataKey, 3, false));
        //原子指标key
        String atomicIndicatorsKey = UUID.randomUUID().toString();
        data.add(setAssetsDirectory(atomicIndicatorsKey, "原子指标", analysisModelKey, 4, false));
        //派生指标key
        String derivedIndicatorsKey = UUID.randomUUID().toString();
        data.add(setAssetsDirectory(derivedIndicatorsKey, "派生指标", analysisModelKey, 4, false));
        //宽表key
        String wideTableKey = UUID.randomUUID().toString();
        data.add(setAssetsDirectory(wideTableKey, "宽表", analysisModelKey, 4, false));

        //调用数仓建模模块接口,获取表名
        ResultEntity<Object> result = client.getDataModelTable(2);
        if (result.code != ResultEnum.SUCCESS.getCode()) {
            return data;
        }
        List<SourceTableDTO> sourceTableList = JSON.parseArray(JSON.toJSONString(result.data), SourceTableDTO.class);
        for (SourceTableDTO item : sourceTableList) {
            for (SourceFieldDTO field : item.fieldList) {
                //原子指标
                if (field.attributeType == FactAttributeEnum.MEASURE.getValue()) {
                    data.add(setAssetsDirectory("", field.fieldName, atomicIndicatorsKey, 5, false));
                }
                //派生指标
                else if (field.attributeType == FactAttributeEnum.DERIVED_INDICATORS.getValue()) {
                    data.add(setAssetsDirectory("", field.fieldName, derivedIndicatorsKey, 5, false));
                }
            }
        }
        ////data.addAll(getWideTableList(wideTableKey));
        return data;
    }

    /**
     * 获取宽表
     *
     * @param wideTableKey
     * @return
     */
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
        queryWrapper.lambda()
                .eq(MetadataMapAtlasPO::getTableType, DataModelTableTypeEnum.WIDE_TABLE.getValue())
                .eq(MetadataMapAtlasPO::getType, EntityTypeEnum.RDBMS_TABLE.getValue());
        List<MetadataMapAtlasPO> poList = metadataMapAtlasMapper.selectList(queryWrapper);
        for (MetadataMapAtlasPO item : poList) {
            Optional<SourceTableDTO> first = collect.stream().filter(e -> e.id == item.tableId).findFirst();
            if (!first.isPresent()) {
                continue;
            }
            data.add(setAssetsDirectory(item.atlasGuid, first.get().tableName, wideTableKey, 5, true));
        }
        return data;
    }

    public AssetsDirectoryDTO setAssetsDirectory(String key, String name,
                                                 String parent, Integer level,
                                                 Boolean skip) {
        AssetsDirectoryDTO dto = new AssetsDirectoryDTO();
        dto.key = key;
        dto.name = name;
        dto.parent = parent;
        //第几级
        dto.level = level;
        //是否可跳转
        dto.skip = skip;
        return dto;
    }

    /**
     * 根据数据源,获取限定名
     *
     * @param dataSourceId
     * @return
     */
    public String getDataSource(int dataSourceId) {
        ResultEntity<DataSourceDTO> resultDataSource = userClient.getFiDataDataSourceById(dataSourceId);
        if (resultDataSource.code != ResultEnum.SUCCESS.getCode() && resultDataSource.data == null) {
            return null;
        }
        return resultDataSource.data.conIp + "_" + resultDataSource.data.conDbname;
    }

}
