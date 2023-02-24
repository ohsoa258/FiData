package com.fisk.datamanagement.mapper;

import com.fisk.common.framework.mybatis.FKBaseMapper;
import com.fisk.datamanagement.entity.BusinessClassificationPO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * @ClassName:
 * @Author: 湖~Tloml
 * @Date: 2023
 * @Copyright: 2023 by 湖~Tloml
 * @Description:
 **/
@Mapper
public interface BusinessClassificationMapper extends FKBaseMapper<BusinessClassificationPO> {

    @Select("select id from tb_business_classification where name = #{name} and del_flag = 1")
    String selectParentId(@Param("name") String name);

    @Update("update tb_business_classification set description = #{model.description} where name = #{model.name} and del_flag = 1")
    int updateByName(@Param("model") BusinessClassificationPO model);

    @Select("SELECT b.`name` FROM tb_metadata_classification_map a LEFT JOIN tb_business_classification b ON a.business_classification_id = b.id WHERE a.metadata_entity_id = #{entityId} and a.del_flag = 1 and b.del_flag = 1")
    List<String> selectClassification(@Param("entityId") Integer entityId);

    @Select("select `name` from tb_business_classification where id != #{guid} and del_flag = #{flag}")
    List<String> selectNameList(@Param("guid") String guid, @Param("flag") int flag);
}
