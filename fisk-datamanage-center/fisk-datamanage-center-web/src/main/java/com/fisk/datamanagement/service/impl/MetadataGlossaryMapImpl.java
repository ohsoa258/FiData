package com.fisk.datamanagement.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.datamanagement.dto.metadataglossarymap.GlossaryAndMetaDatasMapDTO;
import com.fisk.datamanagement.dto.metadataglossarymap.MetadataEntitySimpleDTO;
import com.fisk.datamanagement.entity.MetaDataGlossaryMapPO;
import com.fisk.datamanagement.mapper.MetaDataGlossaryMapMapper;
import com.fisk.datamanagement.service.IMetadataGlossaryMap;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

@Service
public class MetadataGlossaryMapImpl
        extends ServiceImpl<MetaDataGlossaryMapMapper, MetaDataGlossaryMapPO>
        implements IMetadataGlossaryMap {

    @Override
    public Object mapGlossaryWithMetaEntity(GlossaryAndMetaDatasMapDTO dto) {
        //先根据术语id删除所有关联关系
        Integer glossaryId = dto.getGlossaryId();
        LambdaQueryWrapper<MetaDataGlossaryMapPO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MetaDataGlossaryMapPO::getGlossaryId,glossaryId);
        remove(wrapper);

        // 获取本次术语要绑定的元数据集合
        List<MetadataEntitySimpleDTO> metadataEntityIds = dto.getMetadataEntityIds();
        if (CollectionUtils.isEmpty(metadataEntityIds)){
            return null;
        }

        ArrayList<MetaDataGlossaryMapPO> metaDataGlossaryMapPOS = new ArrayList<>();

        for (MetadataEntitySimpleDTO simDto : metadataEntityIds) {
            MetaDataGlossaryMapPO metaDataGlossaryMapPO = new MetaDataGlossaryMapPO();

            metaDataGlossaryMapPO.setMetadataEntityId((int) simDto.getId());
            metaDataGlossaryMapPO.setGlossaryId(glossaryId);
        }


        return null;
    }
}
