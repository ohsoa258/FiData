package com.fisk.dataaccess.mapper;

import com.fisk.common.mybatis.FKBaseMapper;
import com.fisk.dataaccess.entity.EtlIncrementalPO;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * @author Lock
 */
public interface EtlIncrementalMapper extends FKBaseMapper<EtlIncrementalPO> {

    /**
     * 根据tableName查询最近的同步开始结束时间
     *
     * @param tableName tableName
     * @return 返回值
     */
    @Select("select MAX(incremental_objectivescore_start) as incremental_objectivescore_start,MAX(incremental_objectivescore_end) " +
            "as incremental_objectivescore_end from tb_etl_Incremental where object_name=#{object_name} and del_flag=1 and enable_flag=2;")
    EtlIncrementalPO getEtlIncrementalByTableName(@Param("object_name") String tableName);
}
