package com.fisk.datamanagement.mapper;

import com.fisk.common.framework.mybatis.FKBaseMapper;
import com.fisk.datamanagement.dto.classification.BusinessTargetinfoMenuDTO;
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

    @Select("<script> select * from tb_business_targetinfo where pid in <foreach collection='ids' item='id' open='(' separator=',' close=')'> #{id} </foreach> and del_flag = 1 </script>")
    List<BusinessTargetinfoPO> selectClassification(@Param("ids") List<Long> ids );
    @Select("select * from tb_business_targetinfo where id = #{id} and del_flag = 1 ")
    List<BusinessTargetinfoPO> selectClassificationss(@Param("id") String id);

    @Select("<script> select  *  from  (select a.*,b.name as namepid  from  tb_business_targetinfo as a LEFT JOIN tb_business_category as b on a.pid=b.id ) as ab where  id in<foreach collection='ids' item='id' open='(' separator=',' close=')'> #{id} </foreach>  and del_flag = 1 </script>")
    List<Map<String,Object>> selectClassification1(@Param("ids") List<String> tableIds );
    @Select("select  *  from  (select a.*,b.name as namepid  from  tb_business_targetinfo as a LEFT JOIN tb_business_category as b on a.pid=b.id ) as ab where  del_flag = 1 ")
    List<Map<String,Object>> selectClassification2();

    @Select("WITH RECURSIVE MenuHierarchy AS ( SELECT id, name, pid, 1 AS level, CAST(name AS CHAR(200)) AS full_path FROM tb_business_category WHERE pid IS NULL UNION ALL SELECT m.id, m.name, m.pid, mh.level + 1, CONCAT(mh.full_path, '->', m.name) FROM tb_business_category m JOIN MenuHierarchy mh ON m.pid = mh.id ) SELECT id, name, level, full_path FROM MenuHierarchy ORDER BY full_path")
    List<Map<String,Object>> getMenuTreeNames();

    @Select("select * from tb_business_synchronous")
    List<BusinessSynchronousPO> selecttypeClassification();

    @Select("select `name` from tb_business_classification where id != #{guid} and del_flag = #{flag}")
    List<String> selectNameList(@Param("guid") String guid, @Param("flag") int flag);

    @Delete("truncate TABLE tb_business_targetinfo")
    int truncateTable();


    @Select({"<script> " + "${sql}" + "</script>"})
    List<BusinessTargetinfoPO> selectDimensionList(@Param("sql") String sql);

    @Update("update tb_business_extendedfields set parent_business_id = NULL where parent_business_id = #{parentBusinessId} and del_flag = 1")
    int updateParentBusinessId(@Param("parentBusinessId") Integer parentBusinessId);

    @Select("select count(1) from tb_business_targetinfo where del_flag = 1")
    Integer getBusinessTargetinfoTotal();

    List<BusinessTargetinfoMenuDTO> filter(@Param("pids") List<Integer> pids,@Param("where")String where);
}
