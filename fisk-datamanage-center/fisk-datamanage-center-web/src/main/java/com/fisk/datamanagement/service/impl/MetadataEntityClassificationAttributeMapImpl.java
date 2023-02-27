package com.fisk.datamanagement.service.impl;

import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.datamanagement.dto.MetadataEntityClassificationAttributeMapDTO;
import com.fisk.datamanagement.entity.MetadataEntityClassificationAttributePO;
import com.fisk.datamanagement.mapper.MetadataEntityClassificationAttributeMapper;
import com.fisk.datamanagement.service.IMetadataEntityClassificationAttributeMap;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author JianWenYang
 */
public class MetadataEntityClassificationAttributeMapImpl
        extends ServiceImpl<MetadataEntityClassificationAttributeMapper, MetadataEntityClassificationAttributePO>
        implements IMetadataEntityClassificationAttributeMap {

    @Resource
    MetadataEntityClassificationAttributeMapper mapper;


    public List<Map> getMetadataEntityClassificationAttribute(Integer metadataEntityId) {
        List<MetadataEntityClassificationAttributeMapDTO> mapDTOList = mapper.selectClassificationAttribute(metadataEntityId);
        if (CollectionUtils.isEmpty(mapDTOList)) {
            return new ArrayList<>();
        }

        List<Map> mapList = new ArrayList<>();

        Map<String, List<MetadataEntityClassificationAttributeMapDTO>> collect = mapDTOList.stream().collect(Collectors.groupingBy(MetadataEntityClassificationAttributeMapDTO::getClassificationName));
        for (String item : collect.keySet()) {
            Map map = new HashMap();
            map.put("typeName", item);
            map.put("entityGuid", metadataEntityId);
            map.put("entityStatus", "ACTIVE");
            List<MetadataEntityClassificationAttributeMapDTO> mapDTOList1 = collect.get(item);
            if (!CollectionUtils.isEmpty(mapDTOList1)) {
                Map map1 = new HashMap();
                for (MetadataEntityClassificationAttributeMapDTO data : mapDTOList1) {
                    map1.put(data.name, data.value);
                }
                map.put("attributes", map1);
            }
            mapList.add(map);
        }


        return mapList;
    }

}
