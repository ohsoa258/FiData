package com.fisk.mdm.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fisk.mdm.entity.EventLogPO;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author WangYan
 * @date 2022/4/5 19:33
 */
@Mapper
public interface EventLogMapper extends BaseMapper<EventLogPO> {
}
