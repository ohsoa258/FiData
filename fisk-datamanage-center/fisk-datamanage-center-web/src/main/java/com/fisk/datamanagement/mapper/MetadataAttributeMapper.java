package com.fisk.datamanagement.mapper;

import com.fisk.common.framework.mybatis.FKBaseMapper;
import com.fisk.datamanagement.entity.MetadataAttributePO;
import org.apache.ibatis.annotations.Delete;

/**
 * @author JianWenYang
 */
public interface MetadataAttributeMapper extends FKBaseMapper<MetadataAttributePO> {

    @Delete("truncate TABLE  tb_metadata_attribute")
    int truncateTable();
}
