package com.fisk.datamanagement.service.impl;

import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.datamanagement.dto.MetadataEntityClassificationAttributeMapDTO;
import com.fisk.datamanagement.dto.metadataclassificationmap.MetadataClassificationMapInfoDTO;
import com.fisk.datamanagement.entity.MetadataEntityClassificationAttributePO;
import com.fisk.datamanagement.mapper.MetaDataClassificationMapMapper;
import com.fisk.datamanagement.mapper.MetadataEntityClassificationAttributeMapper;
import com.fisk.datamanagement.service.IMetadataEntityClassificationAttributeMap;
import org.springframework.stereotype.Service;

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
public class MetadataEntityClassificationAttributeMapImpl
        extends ServiceImpl<MetadataEntityClassificationAttributeMapper, MetadataEntityClassificationAttributePO>
        implements IMetadataEntityClassificationAttributeMap {

    @Resource
    MetadataEntityClassificationAttributeMapper mapper;
    @Resource
    MetaDataClassificationMapMapper metaDataClassificationMapMapper;


    public List<Map> getMetadataEntityClassificationAttribute(Integer metadataEntityId) {

        List<Map> mapList = new ArrayList<>();

        List<MetadataClassificationMapInfoDTO> metaDataClassificationMap = metaDataClassificationMapMapper.getMetaDataClassificationMap(metadataEntityId);
        if (CollectionUtils.isEmpty(metaDataClassificationMap)) {
            return mapList;
        }

        List<MetadataClassificationMapInfoDTO> classificationMapInfoDTOList = metaDataClassificationMap
                .stream()
                .filter(e -> e.metadataEntityId.equals(metadataEntityId))
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(classificationMapInfoDTOList)) {
            return mapList;
        }

        for (MetadataClassificationMapInfoDTO item : classificationMapInfoDTOList) {
            Map map = new HashMap();
            map.put("typeName", item.name);
            map.put("entityGuid", metadataEntityId);
            map.put("entityStatus", "ACTIVE");

            List<MetadataEntityClassificationAttributeMapDTO> collect = mapper.selectClassificationAttributes(item.metadataEntityId, item.businessClassificationId);

            if (!CollectionUtils.isEmpty(collect)) {
                Map map1 = new HashMap();
                for (MetadataEntityClassificationAttributeMapDTO data : collect) {
                    map1.put(data.attributeName, data.value == null ? "" : data.value);
                }
                map.put("attributes", map1);
            }

            mapList.add(map);
        }

        return mapList;
    }

    public List<MetadataEntityClassificationAttributeMapDTO> getMetadataEntityClassification(Integer metadataEntityId) {
        return mapper.selectClassificationAttribute(metadataEntityId);
    }

}
