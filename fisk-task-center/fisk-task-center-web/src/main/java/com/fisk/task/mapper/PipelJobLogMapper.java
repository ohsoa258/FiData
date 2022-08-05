package com.fisk.task.mapper;

import com.fisk.common.framework.mybatis.FKBaseMapper;
import com.fisk.task.dto.dispatchlog.PipelJobLogVO;
import com.fisk.task.entity.PipelJobLogPO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author cfk
 */
public interface PipelJobLogMapper extends FKBaseMapper<PipelJobLogPO> {

    List<PipelJobLogVO> getPipelJobLogVo(@Param("pipelJobLog") PipelJobLogVO pipelJobLog);

}
