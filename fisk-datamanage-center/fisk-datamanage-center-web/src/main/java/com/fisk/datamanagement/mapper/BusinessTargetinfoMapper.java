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
    @Select("select * from tb_business_targetinfo where id = #{id} and del_flag = 1 ")
    List<BusinessTargetinfoPO> selectClassificationss(@Param("id") String id);


    @Select("select  *  from  (select a.*,b.name as namepid  from  tb_business_targetinfo as a LEFT JOIN tb_business_category as b on a.pid=b.id ) as ab where  pid = #{pid} and indicator_name= #{indicatorname} and del_flag = 1 ")
    List<Map<String,Object>> selectClassification1(@Param("pid") String pid ,@Param("indicatorname") String indicatorname);
    @Select("select  *  from  (select a.*,b.name as namepid  from  tb_business_targetinfo as a LEFT JOIN tb_business_category as b on a.pid=b.id ) as ab where  del_flag = 1 ")
    List<Map<String,Object>> selectClassification2();
    @Select("select  *  from  (select a.*,b.name as namepid  from  tb_business_targetinfo as a LEFT JOIN tb_business_category as b on a.pid=b.id ) as ab where  pid = #{pid} and del_flag = 1 ")
    List<Map<String,Object>> selectClassification3(@Param("pid") String pid );

    @Select("select * from tb_business_synchronous")
    List<BusinessSynchronousPO> selecttypeClassification();

    @Select("select `name` from tb_business_classification where id != #{guid} and del_flag = #{flag}")
    List<String> selectNameList(@Param("guid") String guid, @Param("flag") int flag);

    @Delete("truncate TABLE tb_business_targetinfo")
    int truncateTable();


//    @Select("select * from tb_business_targetinfo where name like '%#{name}%' and del_flag = 1 ")
//    List<BusinessTargetinfoPO> selectClassification1(@Param("name") String name);
    @Select({"<script> " + "${sql}" + "</script>"})
    List<BusinessTargetinfoPO> selectDimensionList(@Param("sql") String sql);

    @Update("update tb_business_extendedfields set parent_business_id = NULL where parent_business_id = #{parentBusinessId} and del_flag = 1")
    int updateParentBusinessId(@Param("parentBusinessId") Integer parentBusinessId);
}
