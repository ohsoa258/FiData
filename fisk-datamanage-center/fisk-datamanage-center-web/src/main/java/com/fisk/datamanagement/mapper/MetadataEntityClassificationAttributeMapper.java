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
            "\ta.metadata_entity_id,\n" +
            "\ta.`value`,\n" +
            "\tb.`name`,\n" +
            "\tc.`name` as classification_name\n" +
            "FROM\n" +
            "\t`tb_metadata_entity_classification_attribute` a\n" +
            "\tLEFT JOIN tb_attribute_type b ON a.attribute_type_id = b.type_id\n" +
            "\tLEFT JOIN tb_business_classification c on a.classification_id = c.id\n" +
            "\twhere a.del_flag = 1")
    List<MetadataEntityClassificationAttributeMapDTO> selectClassificationAttribute(@Param("metadataEntityId") Integer metadataEntityId);

}
