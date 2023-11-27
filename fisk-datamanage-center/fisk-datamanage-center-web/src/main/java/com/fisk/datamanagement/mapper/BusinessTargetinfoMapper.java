package com.fisk.datamanagement.mapper;

import com.fisk.common.framework.mybatis.FKBaseMapper;
import com.fisk.datamanagement.entity.BusinessCategoryPO;
import com.fisk.datamanagement.entity.BusinessSynchronousPO;
import com.fisk.datamanagement.entity.BusinessTargetinfoPO;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Map;

/**
 * @author xgf
 * @date 2023年11月20日 9:49
 */
@Mapper
public interface BusinessTargetinfoMapper extends FKBaseMapper<BusinessTargetinfoPO> {
    @Select("select id from tb_business_targetinfo where name = #{name} and del_flag = 1")
    String selectParentId(@Param("name") String name);

    @Update("update tb_business_targetinfo set description = #{model.description} where name = #{model.name} and del_flag = 1")
    int updateByName(@Param("model") BusinessCategoryPO model);

    @Select("select * from tb_business_targetinfo where pid = #{pid} and del_flag = 1 ")
    List<BusinessTargetinfoPO> selectClassification(@Param("pid") String pid);

    @Select("select * from tb_business_targetinfo where pid = #{pid} and del_flag = 1 ")
    List<Map<String,Object>> selectClassification1(@Param("pid") String pid);


    @Select("select * from tb_business_synchronous")
    List<BusinessSynchronousPO> selecttypeClassification();

    @Select("select `name` from tb_business_classification where id != #{guid} and del_flag = #{flag}")
    List<String> selectNameList(@Param("guid") String guid, @Param("flag") int flag);

    @Delete("truncate TABLE tb_business_targetinfo")
    int truncateTable();


}
