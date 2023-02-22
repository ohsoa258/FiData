package com.fisk.dataservice.mapper;

import com.fisk.common.framework.mybatis.FKBaseMapper;
import com.fisk.dataservice.entity.DataViewAccountPO;
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
public interface DataViewAccountMapper extends FKBaseMapper<DataViewAccountPO> {

    /**
     * 查询视图主题id集合
     * @param viewThemeId
     * @return
     */
    @Select("select id from tb_database_account where view_theme_id = #{viewThemeId}")
    List<Integer> selectIdListByViewThemeId(@Param("viewThemeId") Integer viewThemeId);
}
