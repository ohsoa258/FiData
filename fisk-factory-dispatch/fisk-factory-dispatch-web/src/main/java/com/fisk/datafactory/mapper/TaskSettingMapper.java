package com.fisk.datafactory.mapper;

import com.fisk.common.framework.mybatis.FKBaseMapper;
import com.fisk.datafactory.entity.TaskSettingPO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

/**
 * @author cfk
 */
@Mapper
public interface TaskSettingMapper extends FKBaseMapper<TaskSettingPO> {

    @Update("update tb_task_setting set del_flag = 0 where del_flag = 1 and task_id = #{taskId}")
    void deleteByTaskId(@Param("taskId")long taskId);

}
