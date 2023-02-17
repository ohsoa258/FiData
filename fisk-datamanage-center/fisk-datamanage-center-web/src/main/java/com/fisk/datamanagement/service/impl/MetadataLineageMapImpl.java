package com.fisk.datamanagement.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.framework.exception.FkException;
import com.fisk.datamanagement.dto.metadatalineagemap.MetadataLineageMapDTO;
import com.fisk.datamanagement.entity.MetadataLineageMapPO;
import com.fisk.datamanagement.map.MetadataLineageMap;
import com.fisk.datamanagement.mapper.MetadataLineageMapper;
import com.fisk.datamanagement.service.IMetadataLineageMap;
import org.springframework.stereotype.Service;

/**
 * @author JianWenYang
 */
@Service
public class MetadataLineageMapImpl
        extends ServiceImpl<MetadataLineageMapper, MetadataLineageMapPO>
        implements IMetadataLineageMap {

    @Override
    public Long addMetadataLineageMap(MetadataLineageMapDTO dto) {
        MetadataLineageMapPO po = MetadataLineageMap.INSTANCES.dtoToPo(dto);
        boolean save = this.save(po);
        if (!save) {
            throw new FkException(ResultEnum.SAVE_DATA_ERROR);
        }
        return po.id;
    }

}
