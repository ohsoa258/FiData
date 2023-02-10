package com.fisk.datamanagement.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.framework.exception.FkException;
import com.fisk.datamanagement.entity.MetadataEntityTypePO;
import com.fisk.datamanagement.mapper.MetadataEntityTypeMapper;
import com.fisk.datamanagement.service.IMetadataEntityType;
import org.springframework.stereotype.Service;

/**
 * @author JianWenYang
 */
@Service
public class MetadataEntityTypeImpl
        extends ServiceImpl<MetadataEntityTypeMapper, MetadataEntityTypePO>
        implements IMetadataEntityType {

    @Override
    public Integer getTypeId(String type) {
        MetadataEntityTypePO po = this.query().eq("type", type).one();
        if (po == null) {
            throw new FkException(ResultEnum.DATA_NOTEXISTS);
        }

        return (int) po.id;
    }

}
