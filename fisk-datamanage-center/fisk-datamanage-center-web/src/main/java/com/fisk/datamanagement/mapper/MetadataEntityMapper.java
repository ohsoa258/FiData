package com.fisk.datamanagement.mapper;

import com.fisk.common.framework.mybatis.FKBaseMapper;
import com.fisk.datamanagement.entity.MetadataEntityPO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author JianWenYang
 */
@Mapper
public interface MetadataEntityMapper extends FKBaseMapper<MetadataEntityPO> {

    /**
     * 获取实体集合
     *
     * @param type
     * @return
     */
    List<MetadataEntityPO> selectMetadataEntity(@Param("type") Integer type);

}
