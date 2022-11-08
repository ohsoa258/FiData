package com.fisk.task.mapper;

import com.fisk.common.framework.mybatis.FKBaseMapper;
import com.fisk.task.dto.dispatchlog.PipelTaskLogVO;
import com.fisk.task.entity.PipelTaskLogPO;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * @author cfk
 */
public interface PipelTaskLogMapper extends FKBaseMapper<PipelTaskLogPO> {


    List<PipelTaskLogVO> getByTaskId(@Param("taskId") String taskId, @Param("jobTraceId") String jobTraceId);
}
