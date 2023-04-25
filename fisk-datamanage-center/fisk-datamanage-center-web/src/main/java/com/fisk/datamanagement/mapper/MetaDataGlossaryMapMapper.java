package com.fisk.datamanagement.mapper;

import com.fisk.common.framework.mybatis.FKBaseMapper;
import com.fisk.datamanagement.dto.metadataglossarymap.MetaDataGlossaryMapDTO;
import com.fisk.datamanagement.entity.MetaDataGlossaryMapPO;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * @ClassName:
 * @Author: 湖~Tloml
 * @Date: 2023
 * @Copyright: 2023 by 湖~Tloml
 * @Description:
 **/
@Mapper
public interface MetaDataGlossaryMapMapper extends FKBaseMapper<MetaDataGlossaryMapPO> {

    @Select("SELECT b.`name` FROM tb_metadata_glossary_map a LEFT JOIN tb_glossary b ON a.glossary_id = b.id WHERE a.metadata_entity_id = #{entityId} and a.del_flag = 1 and b.del_flag = 1")
    List<String> selectGlossary(@Param("entityId") Integer entityId);

    /**
     * 获取实体关联术语
     *
     * @param entityId
     * @return
     */
    @Select("SELECT\n" +
            "\ta.metadata_entity_id,\n" +
            "\ta.glossary_id,\n" +
            "\tb.`name` as glossary_name\n" +
            "FROM\n" +
            "\ttb_metadata_glossary_map a\n" +
            "\tLEFT JOIN tb_glossary b ON a.glossary_id = b.id \n" +
            "WHERE\n" +
            "\ta.metadata_entity_id = #{entityId} and a.del_flag = 1 and b.del_flag = 1")
    List<MetaDataGlossaryMapDTO> getEntityGlossary(@Param("entityId") Integer entityId);

    @Delete("truncate TABLE tb_metadata_glossary_map ")
    int truncateTable();

}
