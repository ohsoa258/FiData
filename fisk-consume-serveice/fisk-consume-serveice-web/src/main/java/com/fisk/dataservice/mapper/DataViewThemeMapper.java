package com.fisk.dataservice.mapper;

import com.fisk.common.framework.mybatis.FKBaseMapper;
import com.fisk.dataservice.entity.DataViewThemePO;
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
public interface DataViewThemeMapper extends FKBaseMapper<DataViewThemePO> {

    /**
     * 获取视图主题简称
     * @return
     * @param flag
     */
    @Select("select theme_abbr from tb_view_theme where del_flag = #{flag}")
    List<String> getAbbreviation(@Param("flag") int flag);

    /**
     * 查询视图主题id
     * @param themeName 视图主题名称
     * @param value
     * @return
     */
    @Select("select id from tb_view_theme where theme_name = #{themeName} and del_flag = #{value}")
    Integer selectViewThemeId(@Param("themeName") String themeName, @Param("value") int value);

    /**
     * 查询数据视图 主题的目标dbId
     * @param viewThemeId
     * @return
     */
    @Select("select target_db_id from tb_view_theme where id = #{viewThemeId}")
    Integer selectDbId(@Param("viewThemeId") Integer viewThemeId);
}
