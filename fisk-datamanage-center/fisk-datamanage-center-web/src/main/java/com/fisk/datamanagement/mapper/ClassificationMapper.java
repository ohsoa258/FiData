package com.fisk.datamanagement.mapper;

import com.fisk.common.framework.mybatis.FKBaseMapper;
import com.fisk.datamanagement.entity.AttributeTypePO;
import com.fisk.datamanagement.entity.ClassificationPO;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;

/**
 * @ClassName:
 * @Author: 湖~Zero
 * @Date: 2023
 * @Copyright: 2023 by 湖~Zero
 * @Description:
 **/
@Mapper
public interface ClassificationMapper extends FKBaseMapper<ClassificationPO> {


    @Delete("truncate TABLE tb_metadata_classification_map")
    int truncateTable();

}
