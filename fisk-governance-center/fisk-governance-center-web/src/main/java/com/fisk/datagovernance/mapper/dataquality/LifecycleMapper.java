package com.fisk.datagovernance.mapper.dataquality;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fisk.common.framework.mybatis.FKBaseMapper;
import com.fisk.datagovernance.entity.dataquality.LifecyclePO;
import com.fisk.datagovernance.vo.dataquality.lifecycle.LifecycleVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * @author dick
 * @version 1.0
 * @description 生命周期
 * @date 2022/3/23 12:42
 */
@Mapper
public interface LifecycleMapper extends FKBaseMapper<LifecyclePO> {
    /**
     * 查询数据校验分页列表
     *
     * @param page    分页信息
     * @param keyword where条件
     * @param tableUnique 表名称/表Id
     * @return 查询结果
     */
    Page<LifecycleVO> getAll(Page<LifecycleVO> page,
                             @Param("datasourceId") int datasourceId,
                             @Param("tableUnique") String tableUnique,
                             @Param("keyword") String keyword);
}