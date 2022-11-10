package com.fisk.task.mapper;

import com.fisk.common.framework.mybatis.FKBaseMapper;
import com.fisk.task.dto.dispatchlog.PipelStageLogVO;
import com.fisk.task.entity.PipelStageLogPO;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * @author cfk
 */
public interface PipelStateLogMapper extends FKBaseMapper<PipelStageLogPO> {

    @Select("select * from tb_pipel_stage_log where task_trace_id =#{taskTraceId} order by create_time desc limit 100")
    List<PipelStageLogVO> getPipelStateLogs(String taskTraceId);
}
