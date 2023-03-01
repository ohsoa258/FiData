package com.fisk.datamanagement.mapper;

import com.fisk.common.framework.mybatis.FKBaseMapper;
import com.fisk.datamanagement.entity.MetadataEntityPO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

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

    /**
     * 根据类型获取实体集合
     *
     * @param typeId
     * @return
     */
    @Select("select * from tb_metadata_entity where (description != 'stg' or description is null) and del_flag = 1 and type_id = #{typeId} ")
    List<MetadataEntityPO> getMetadataEntityByType(@Param("typeId") Integer typeId);

}
