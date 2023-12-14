package com.fisk.datamanagement.mapper;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fisk.common.framework.mybatis.FKBaseMapper;
import com.fisk.datamanagement.dto.classification.BusinessCategoryDefsDTO;
import com.fisk.datamanagement.entity.BusinessCategoryPO;
import com.fisk.datamanagement.entity.BusinessClassificationPO;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * @author xgf
 * @date 2023年11月20日 9:49
 */
@Mapper
public interface BusinessCategoryMapper   extends FKBaseMapper<BusinessCategoryPO> {
    @Select("select id from tb_business_category where name = #{name} and del_flag = 1")
    String selectParentId(@Param("name") String name);


    @Select("select * from tb_business_category where pid = #{pid} and del_flag = 1")
    List<BusinessCategoryPO> selectParentpId(@Param("pid") String pid);

    @Update("update tb_business_classification set description = #{model.description} where name = #{model.name} and del_flag = 1")
    int updateByName(@Param("model") BusinessCategoryPO model);

    @Select("select * from tb_business_category ")
    List<BusinessCategoryPO> selectClassification();

    @Select("select `name` from tb_business_classification where id != #{guid} and del_flag = #{flag}")
    List<String> selectNameList(@Param("guid") String guid, @Param("flag") int flag);

    @Delete("truncate TABLE tb_business_classification")
    int truncateTable();


}
