package com.fisk.task.mapper;

import com.fisk.common.framework.mybatis.FKBaseMapper;
import com.fisk.task.dto.dispatchlog.LogStatisticsVO;
import com.fisk.task.dto.dispatchlog.PipelLogVO;
import com.fisk.task.entity.PipelLogPO;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * @author cfk
 */
public interface PipelLogMapper extends FKBaseMapper<PipelLogPO> {

    @Select("select * from tb_pipel_log where pipel_id=#{pipelLog.pipelId} and del_flag = 1 order by create_time desc")
    List<PipelLogVO> getPipelLogs(@Param("pipelLog") PipelLogVO pipelLog);


    List<LogStatisticsVO> getStatisticsLog(@Param("lookday") int lookday, @Param("pipelId") int pipelId, @Param("result") String result);
}
