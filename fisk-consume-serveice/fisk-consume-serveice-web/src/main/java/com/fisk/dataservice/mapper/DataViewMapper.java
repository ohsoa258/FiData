package com.fisk.dataservice.mapper;

import com.fisk.common.framework.mybatis.FKBaseMapper;
import com.fisk.dataservice.entity.DataViewPO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * @ClassName:
 * @Author: 湖~Zero
 * @Date: 2023
 * @Copyright: 2023 by 湖~Zero
 * @Description:
 **/
@Mapper
public interface DataViewMapper extends FKBaseMapper<DataViewPO> {
    @Select("select view_theme_id from tb_view where id = #{id} and del_flag = 1")
    String selectThemeId(@Param("id") long id);

    @Select("SELECT t.theme_name from tb_view as v left join tb_view_theme as t on v.view_theme_id = t.id " +
            "WHERE v.name = #{name} and t.target_db_id = #{targetDbId} and t.del_flag = 1 and v.del_flag = 1 ")
    String selectAbbrName(@Param("targetDbId") Integer targetDbId, @Param("name") String name);

    @Select("SELECT * FROM tb_view WHERE del_flag=1 AND view_theme_id = #{viewThemeId} AND name = #{viewName} AND del_flag = #{value}")
    DataViewPO selectId(@Param("viewThemeId")Integer viewThemeId, @Param("viewName") String viewName, @Param("value") int value);
}
