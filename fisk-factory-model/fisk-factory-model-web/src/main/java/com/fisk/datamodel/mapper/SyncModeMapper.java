package com.fisk.datamodel.mapper;

import com.fisk.common.framework.mybatis.FKBaseMapper;
import com.fisk.datamodel.entity.SyncModePO;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author JianWenYang
 */
@Mapper
public interface SyncModeMapper extends FKBaseMapper<SyncModePO> {
}
