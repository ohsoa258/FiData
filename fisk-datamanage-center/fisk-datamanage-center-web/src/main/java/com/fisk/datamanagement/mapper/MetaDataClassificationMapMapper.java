package com.fisk.datamanagement.mapper;

import com.fisk.common.framework.mybatis.FKBaseMapper;
import com.fisk.datamanagement.dto.metadataclassificationmap.MetadataClassificationMapInfoDTO;
import com.fisk.datamanagement.entity.MetadataClassificationMapPO;
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
public interface MetaDataClassificationMapMapper extends FKBaseMapper<MetadataClassificationMapPO> {

    /**
     * 获取实体关联业务分类数据
     *
     * @return
     */
    @Select("SELECT\n" +
            "\ta.business_classification_id,\n" +
            "\ta.metadata_entity_id,\n" +
            "\tb.`name` \n" +
            "FROM\n" +
            "\t`tb_metadata_classification_map` a\n" +
            "\tLEFT JOIN tb_business_classification b ON a.business_classification_id = b.id\n" +
            "\tLEFT JOIN tb_metadata_entity c on a.metadata_entity_id = c.id\n" +
            "WHERE\n" +
            "\ta.del_flag = 1 \n" +
            "\tAND b.del_flag = 1 \n" +
            "\tAND b.pid IS NOT NULL\n")
    List<MetadataClassificationMapInfoDTO> getMetaDataClassificationMap();

    @Select("SELECT\n" +
            "\ta.business_classification_id,\n" +
            "\ta.metadata_entity_id,\n" +
            "\tb.`name` \n" +
            "FROM\n" +
            "\t`tb_metadata_classification_map` a\n" +
            "\tLEFT JOIN tb_business_classification b ON a.business_classification_id = b.id\n" +
            "WHERE\n" +
            "\ta.del_flag = 1 \n" +
            "\ta.metadata_entity_id = #{entityId} \n" +
            "\tAND b.del_flag = 1 \n" +
            "\tAND b.pid IS NOT NULL\n")
    List<MetadataClassificationMapInfoDTO> getMetaDataClassificationMap(@Param("entityId") Integer entityId);

}
