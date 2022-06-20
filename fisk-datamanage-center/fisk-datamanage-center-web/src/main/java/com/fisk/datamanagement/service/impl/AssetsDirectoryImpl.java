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
import com.fisk.datamodel.dto.tableconfig.SourceTableDTO;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

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

    public AssetsDirectoryDTO setAssetsDirectory(String key, String name, String parent) {
        AssetsDirectoryDTO dto = new AssetsDirectoryDTO();
        dto.key = key;
        dto.name = name;
        dto.parent = parent;
        return dto;
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

        return data;
    }

}
