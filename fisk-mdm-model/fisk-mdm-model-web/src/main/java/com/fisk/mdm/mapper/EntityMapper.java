package com.fisk.mdm.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fisk.mdm.entity.EntityPO;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author WangYan
 * @date 2022/4/2 17:48
 */
@Mapper
public interface EntityMapper extends BaseMapper<EntityPO> {
}
