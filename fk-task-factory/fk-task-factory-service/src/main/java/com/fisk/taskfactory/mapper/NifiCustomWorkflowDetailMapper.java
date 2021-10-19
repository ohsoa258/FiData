package com.fisk.taskfactory.mapper;

import com.fisk.common.mybatis.FKBaseMapper;
import com.fisk.taskfactory.entity.NifiComponentsPO;
import com.fisk.taskfactory.entity.NifiCustomWorkflowDetailPO;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author Lock
 */
@Mapper
public interface NifiCustomWorkflowDetailMapper extends FKBaseMapper<NifiCustomWorkflowDetailPO> {
}
