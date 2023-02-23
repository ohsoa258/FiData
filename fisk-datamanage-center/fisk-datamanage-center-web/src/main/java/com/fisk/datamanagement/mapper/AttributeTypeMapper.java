package com.fisk.datamanagement.mapper;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fisk.common.framework.mybatis.FKBaseMapper;
import com.fisk.datamanagement.entity.AttributeTypePO;
import com.fisk.datamanagement.entity.BusinessClassificationPO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * @ClassName:
 * @Author: 湖~Zero
 * @Date: 2023
 * @Copyright: 2023 by 湖~Zero
 * @Description:
 **/
@Mapper
public interface AttributeTypeMapper extends FKBaseMapper<AttributeTypePO> {

    @Select("select name from tb_attribute_type where type_id = #{typeId}")
    String selectTypeName(@Param("typeId") Integer typeId);
}
