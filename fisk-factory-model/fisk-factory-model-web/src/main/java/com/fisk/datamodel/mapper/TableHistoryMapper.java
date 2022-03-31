package com.fisk.datamodel.mapper;

import com.fisk.common.framework.mybatis.FKBaseMapper;
import com.fisk.datamodel.entity.TableHistoryPO;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author JianWenYang
 */
@Mapper
public interface TableHistoryMapper extends FKBaseMapper<TableHistoryPO> {
}
