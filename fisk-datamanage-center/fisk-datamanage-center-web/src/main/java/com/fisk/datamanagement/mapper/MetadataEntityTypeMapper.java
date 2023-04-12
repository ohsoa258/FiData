package com.fisk.datamanagement.mapper;

import com.fisk.common.framework.mybatis.FKBaseMapper;
import com.fisk.datamanagement.entity.MetadataEntityTypePO;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * @author JianWenYang
 */
public interface MetadataEntityTypeMapper extends FKBaseMapper<MetadataEntityTypePO> {

    @Select("select type from tb_metadata_entity_type where id = #{typeId} and del_flag = 1")
    String selectNameById(@Param("typeId") Integer typeId);
}
