package com.fisk.common.mybatis;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * @author gy
 */
public interface FKBaseMapper<T> extends BaseMapper<T> {

    /**
     * 删除数据，并触发自动填充
     * @param T
     * @return
     */
    int deleteByIdWithFill(T model);
}
