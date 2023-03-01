package com.fisk.datamanagement.mapper;

import com.fisk.common.framework.mybatis.FKBaseMapper;
import com.fisk.datamanagement.dto.MetadataEntityClassificationAttributeMapDTO;
import com.fisk.datamanagement.entity.MetadataEntityClassificationAttributePO;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * @author JianWenYang
 */
public interface MetadataEntityClassificationAttributeMapper
        extends FKBaseMapper<MetadataEntityClassificationAttributePO> {

    /**
     * 获取实体关联业务分类集合
     *
     * @param metadataEntityId
     * @return
     */
    @Select("SELECT\n" +
            "\ta.attribute_type_id,\n" +
            "\ta.metadata_entity_id\n" +
            "\t,a.`value`\n" +
            "\t,b.`name`\n" +
            "\t,d.`name` AS classification_name \n" +
            "FROM\n" +
            "\ttb_metadata_entity_classification_attribute a\n" +
            "\tLEFT JOIN tb_attribute_type b ON a.attribute_type_id = b.type_id\n" +
            "\tLEFT JOIN tb_classification c ON b.type_id = c.attribute_type_id \n" +
            "\tAND c.business_classification_id = a.classification_id and c.del_flag = 1\n" +
            "\tLEFT JOIN tb_business_classification d ON c.business_classification_id = d.id \n" +
            "WHERE\n" +
            "\ta.metadata_entity_id = #{metadataEntityId} \n" +
            "\tAND a.del_flag = 1 AND c.del_flag = 1 and d.del_flag = 1")
    List<MetadataEntityClassificationAttributeMapDTO> selectClassificationAttribute(@Param("metadataEntityId") Integer metadataEntityId);

    /**
     * 获取实体关联业务分类集合
     *
     * @param metadataEntityId
     * @return
     */
    @Select("SELECT\n" +
            "\ta.classification_id,\n" +
            "\ta.metadata_entity_id,\n" +
            "\tc.`name`\n" +
            "FROM\n" +
            "\t`tb_metadata_entity_classification_attribute` a\n" +
            "\tLEFT JOIN tb_attribute_type c ON a.attribute_type_id = c.type_id\n" +
            "\t\n" +
            "\twhere a.classification_id = #{classificationId} and a.metadata_entity_id = #{metadataEntityId} and a.del_flag = 1 \n")
    List<MetadataEntityClassificationAttributeMapDTO> selectClassificationAttributes(@Param("metadataEntityId") Integer metadataEntityId,
                                                                                     @Param("classificationId") Integer classificationId);


}
