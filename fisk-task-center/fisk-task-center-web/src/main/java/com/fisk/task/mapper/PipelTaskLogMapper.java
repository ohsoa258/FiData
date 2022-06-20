package com.fisk.task.mapper;

import com.fisk.common.framework.mybatis.FKBaseMapper;
import com.fisk.task.dto.dispatchlog.PipelTaskLogVO;
import com.fisk.task.entity.PipelTaskLogPO;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * @author cfk
 */
public interface PipelTaskLogMapper extends FKBaseMapper<PipelTaskLogPO> {

    @Select("select * from tb_pipel_task_log where task_id=#{taskId}")
    List<PipelTaskLogVO> getByTaskId(String taskId);
}
