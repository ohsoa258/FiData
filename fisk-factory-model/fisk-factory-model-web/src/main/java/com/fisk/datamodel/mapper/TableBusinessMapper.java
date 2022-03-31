package com.fisk.datamodel.mapper;

import com.fisk.common.framework.mybatis.FKBaseMapper;
import com.fisk.datamodel.entity.TableBusinessPO;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author JianWenYang
 */
@Mapper
public interface TableBusinessMapper extends FKBaseMapper<TableBusinessPO> {
}
