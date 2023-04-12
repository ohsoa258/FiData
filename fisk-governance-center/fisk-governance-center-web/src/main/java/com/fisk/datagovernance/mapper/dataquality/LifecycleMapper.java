package com.fisk.datagovernance.mapper.dataquality;

import com.fisk.common.framework.mybatis.FKBaseMapper;
import com.fisk.datagovernance.entity.dataquality.LifecyclePO;
import com.fisk.datagovernance.vo.dataquality.lifecycle.LifecycleVO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * @author dick
 * @version 1.0
 * @description 生命周期
 * @date 2022/3/23 12:42
 */
@Mapper
public interface LifecycleMapper extends FKBaseMapper<LifecyclePO> {
    /**
     * 查询回收规则列表
     *
     * @return 查询结果
     */
    List<LifecycleVO> getAllRule();
}