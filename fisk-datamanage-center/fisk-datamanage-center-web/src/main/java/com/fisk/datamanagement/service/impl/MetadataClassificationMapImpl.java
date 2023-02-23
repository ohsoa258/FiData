package com.fisk.datamanagement.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.datamanagement.entity.MetadataClassificationMapPO;
import com.fisk.datamanagement.mapper.MetadataClassificationMapper;
import com.fisk.datamanagement.service.IMetadataClassificationMap;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author JianWenYang
 */
@Service
public class MetadataClassificationMapImpl
        extends ServiceImpl<MetadataClassificationMapper, MetadataClassificationMapPO>
        implements IMetadataClassificationMap {

    public List<Integer> getMetadataClassification(Integer entityId) {
        List<MetadataClassificationMapPO> list = this.query().eq("metadata_entity_id", entityId).select("business_classification_id").list();
        if (CollectionUtils.isEmpty(list)) {
            return new ArrayList<>();
        }

        return list.stream().map(e -> e.metadataEntityId).collect(Collectors.toList());
    }

    public List<Integer> getMetadataEntity(Integer classificationId) {
        List<MetadataClassificationMapPO> list = this.query()
                .eq("business_classification_id", classificationId)
                .select("metadata_entity_id")
                .list();
        if (CollectionUtils.isEmpty(list)) {
            return new ArrayList<>();
        }

        return list.stream().map(e -> e.metadataEntityId).collect(Collectors.toList());
    }

    public List<Integer> getMetadataEntity(Integer classificationId, Integer pageIndex, Integer pageSize) {
        List<MetadataClassificationMapPO> list = this.query()
                .eq("business_classification_id", classificationId)
                .select("metadata_entity_id")
                .list();
        if (CollectionUtils.isEmpty(list)) {
            return new ArrayList<>();
        }

        return list.stream()
                .skip((pageIndex - 1) * pageSize)
                .limit(pageSize)
                .map(e -> e.metadataEntityId).collect(Collectors.toList());
    }

}
