package com.fisk.task.mapper;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fisk.task.entity.TBETLIncrementalPO;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author: DennyHui
 * CreateTime: 2021/9/4 16:39
 * Description:
 */

public interface TBETLIncrementalMapper extends BaseMapper<TBETLIncrementalPO> {
    /**
     * 根据tableName查询最近的同步开始结束时间
     *
     * @param tableName tableName
     * @return 返回值
     */
    @Select("select MAX(incremental_objectivescore_start) as incremental_objectivescore_start,MAX(incremental_objectivescore_end) " +
            "as incremental_objectivescore_end from tb_etl_Incremental where object_name=#{object_name} and del_flag=1 and enable_flag=2;")
    Map<String, Date> getEtlIncrementalByTableName(@Param("object_name") String tableName);

    @Select("select * from tb_etl_Incremental where object_name=#{object_name} and del_flag=1")
    List<TBETLIncrementalPO> getEtlIncrementalList(@Param("object_name") String tableName);

    @Delete("delete from tb_etl_Incremental where object_name=#{object_name} and del_flag=1")
    int delEtlIncrementalList(@Param("object_name") String tableName);
}
