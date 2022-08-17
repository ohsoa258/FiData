package com.fisk.task.mapper;

import com.fisk.common.framework.mybatis.FKBaseMapper;
import com.fisk.task.po.TableNifiSettingPO;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * @author cfk
 */
public interface TableNifiSettingMapper extends FKBaseMapper<TableNifiSettingPO> {

    /**
     * 获取流程信息
     *
     * @param tableId 表id
     * @param type    表类别
     * @return 返回值
     */
    @Select("select * from tb_table_nifi_setting where table_id=#{tableId} and type=#{type} and  del_flag=1")
    TableNifiSettingPO getByTableId(@Param("tableId") long tableId, @Param("type") long type);
}
