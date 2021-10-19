package com.fisk.taskfactory.mapper;

import com.fisk.common.mybatis.FKBaseMapper;
import com.fisk.taskfactory.entity.NifiComponentsPO;
import com.fisk.taskfactory.entity.TaskSchedulePO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * @author Lock
 */
@Mapper
public interface NifiComponentsMapper extends FKBaseMapper<NifiComponentsPO> {
}
