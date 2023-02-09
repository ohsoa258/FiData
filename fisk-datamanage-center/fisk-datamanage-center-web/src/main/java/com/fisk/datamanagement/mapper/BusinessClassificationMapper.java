package com.fisk.datamanagement.mapper;

import com.fisk.common.framework.mybatis.FKBaseMapper;
import com.fisk.datamanagement.dto.businessclassification.BusinessClassificationDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * @ClassName:
 * @Author: 湖~Tloml
 * @Date: 2023
 * @Copyright: 2023 by 湖~Tloml
 * @Description:
 **/
@Mapper
public interface BusinessClassificationMapper extends FKBaseMapper<BusinessClassificationDTO> {

    @Select("select id from tb_business_classification where name = #{name} and del_flag = 1")
    String selectParentId(@Param("name") String name);
}
