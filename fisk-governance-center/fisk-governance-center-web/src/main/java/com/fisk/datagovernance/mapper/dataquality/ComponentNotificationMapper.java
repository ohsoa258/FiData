package com.fisk.datagovernance.mapper.dataquality;

import com.fisk.common.mybatis.FKBaseMapper;
import com.fisk.datagovernance.entity.dataquality.ComponentNotificationPO;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author dick
 * @version 1.0
 * @description 组件通知关联
 * @date 2022/3/23 12:42
 */
@Mapper
public interface ComponentNotificationMapper extends FKBaseMapper<ComponentNotificationPO> {
}