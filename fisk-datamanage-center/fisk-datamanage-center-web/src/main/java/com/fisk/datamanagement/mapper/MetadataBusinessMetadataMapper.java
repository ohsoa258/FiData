package com.fisk.datamanagement.mapper;

import com.fisk.common.framework.mybatis.FKBaseMapper;
import com.fisk.datamanagement.dto.metadatabusinessmetadatamap.MetadataBusinessInfoDTO;
import com.fisk.datamanagement.entity.MetadataBusinessMetadataMapPO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * @author JianWenYang
 */
@Mapper
public interface MetadataBusinessMetadataMapper extends FKBaseMapper<MetadataBusinessMetadataMapPO> {

    /**
     * 查询业务元数据数据
     *
     * @param entityId
     * @return
     */
    @Select("SELECT\n" +
            "\tb.attribute_name,\n" +
            "\tb.attribute_cn_name,\n" +
            "\tb.business_metadata_cn_name,\n" +
            "\tb.business_metadata_name,\n" +
            "\ta.`value` \n" +
            "FROM\n" +
            "\t`tb_metadata_business_metadata_map` a\n" +
            "\tLEFT JOIN tb_business_metadata_config b ON a.business_metadata_id = b.id \n" +
            "WHERE\n" +
            "\ta.metadata_entity_id = #{entityId}\n" +
            "\tAND\n" +
            "\ta.del_flag = 1 \n" +
            "\tAND b.del_flag = 1")
    List<MetadataBusinessInfoDTO> selectMetadataBusiness(@Param("entityId") Integer entityId);


}
