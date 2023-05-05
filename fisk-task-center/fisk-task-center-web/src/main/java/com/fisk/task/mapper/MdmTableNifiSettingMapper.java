package com.fisk.task.mapper;

import com.fisk.common.framework.mybatis.FKBaseMapper;
import com.fisk.task.po.mdm.MdmTableNifiSettingPO;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * @author wangjian
 */
public interface MdmTableNifiSettingMapper extends FKBaseMapper<MdmTableNifiSettingPO> {

    /**
     * 获取流程信息
     *
     * @param tableId 表id
     * @param type    表类别
     * @return 返回值
     */
    @Select("select * from tb_mdm_table_nifi_setting where table_access_id=#{tableId} and type=#{type} and  del_flag=1")
    MdmTableNifiSettingPO getByTableId(@Param("tableId") long tableId, @Param("type") long type);
}
