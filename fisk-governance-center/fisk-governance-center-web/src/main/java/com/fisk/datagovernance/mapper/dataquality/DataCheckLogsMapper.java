package com.fisk.datagovernance.mapper.dataquality;

import com.fisk.common.framework.mybatis.FKBaseMapper;
import com.fisk.datagovernance.entity.dataquality.DataCheckLogsPO;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author dick
 * @version 1.0
 * @description 数据校验日志
 * @date 2022/3/23 12:42
 */
@Mapper
public interface DataCheckLogsMapper extends FKBaseMapper<DataCheckLogsPO> {
}