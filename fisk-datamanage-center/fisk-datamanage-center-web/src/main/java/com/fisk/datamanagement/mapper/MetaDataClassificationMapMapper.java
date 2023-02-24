package com.fisk.datamanagement.mapper;

import com.fisk.common.framework.mybatis.FKBaseMapper;
import com.fisk.datamanagement.dto.metadataclassificationmap.MetadataClassificationMapInfoDTO;
import com.fisk.datamanagement.entity.MetadataClassificationMapPO;
import org.apache.ibatis.annotations.Mapper;
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
    @Select("SELECT a.business_classification_id,a.metadata_entity_id,b.`name` FROM `tb_metadata_classification_map` a left join tb_business_classification b on a.business_classification_id = b.id\n" +
            "where a.del_flag = 1 and b.del_flag = 1")
    List<MetadataClassificationMapInfoDTO> getMetaDataClassificationMap();

}
