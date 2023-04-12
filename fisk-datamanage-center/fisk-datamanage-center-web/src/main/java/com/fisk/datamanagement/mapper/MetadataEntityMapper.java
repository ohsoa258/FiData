package com.fisk.datamanagement.mapper;

import com.fisk.common.framework.mybatis.FKBaseMapper;
import com.fisk.datamanagement.dto.metadataentity.MetadataEntityDTO;
import com.fisk.datamanagement.dto.search.EntitiesDTO;
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


    /**
     * 获取抽取详细
     * @param guid
     * @return
     */
    MetadataEntityDTO getProcess(@Param("guid")String guid);


    List<EntitiesDTO> searchEntitys(@Param("query")String query, @Param("offset") Integer offset, @Param("limit") Integer limit);

    /**
     * 根据限定名查询等于列的元数据
     * @param qualifiedName
     * @return
     */
    @Select("SELECT tb2.id,tb2.`name`,tb2.display_name,tb2.qualified_name,tb2.type_id,tb2.parent_id\n" +
            "FROM tb_metadata_entity tb \n" +
            "LEFT JOIN tb_metadata_entity tb2 ON tb.id = tb2.parent_id\n" +
            "WHERE  tb2.qualified_name LIKE CONCAT('%',#{qualifiedName},'%')  AND tb2.type_id = 6")
    List<MetadataEntityPO> queryFildes(@Param("qualifiedName")String qualifiedName);
}
