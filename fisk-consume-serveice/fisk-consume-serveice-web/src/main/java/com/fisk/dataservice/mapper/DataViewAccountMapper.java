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
     *
     * @param flag
     * @param viewThemeId
     * @return
     */
    @Select("select id from tb_database_account where view_theme_id = #{viewThemeId} and del_flag = #{flag}")
    List<Integer> selectIdListByViewThemeId(@Param("viewThemeId") Integer viewThemeId, @Param("flag") Integer flag);

    @Select("select a.account_name from tb_database_account as a right join tb_view_theme as v " +
            "on v.target_db_id = #{id} and a.view_theme_id = v.id where a.del_flag = #{value}")
    List<String> selectNameList(@Param("id") Integer id, @Param("value") int value);

    @Select("select a.* from tb_database_account as a right join tb_view_theme as v on v.target_db_id = #{id} and a.view_theme_id = v.id \n" +
            "where a.del_flag = 1 and a.account_name = 'sjj1'\n")
    DataViewAccountPO selectUserInfo(Integer viewThemeId, String accountName, Integer id);
}
