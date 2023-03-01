package com.fisk.datamanagement.mapper;

import com.fisk.common.framework.mybatis.FKBaseMapper;
import com.fisk.datamanagement.entity.MetadataLabelMapPO;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author JianWenYang
 */
@Mapper
public interface MetadataLabelMapper extends FKBaseMapper<MetadataLabelMapPO> {
}
