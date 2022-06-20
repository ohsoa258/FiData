package com.fisk.task.mapper;

import com.fisk.common.framework.mybatis.FKBaseMapper;
import com.fisk.task.dto.dispatchlog.PipelJobLogVO;
import com.fisk.task.entity.PipelJobLogPO;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * @author cfk
 */
public interface PipelJobLogMapper extends FKBaseMapper<PipelJobLogPO> {

    @Select("select * from tb_pipel_job_log where pipel_id=#{pipelJobLog.pipelId} and del_flag=1 ORDER BY create_time desc limit 100")
    List<PipelJobLogVO> getPipelJobLogVo(PipelJobLogVO pipelJobLog);

}
