package com.fisk.datamanagement.mapper;

import com.fisk.common.framework.mybatis.FKBaseMapper;
import com.fisk.datamanagement.entity.MetaDataGlossaryMapPO;
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

}
