package com.fisk.system.mapper;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fisk.common.framework.mybatis.FKBaseMapper;
import com.fisk.system.dto.datasource.DataSourceDTO;
import com.fisk.system.dto.datasource.DataSourcePageDTO;
import com.fisk.system.entity.DataSourcePO;
import org.apache.ibatis.annotations.*;

/**
 * 数据源连接mapper
 *
 * @author dick
 */
@Mapper
public interface DataSourceMapper extends FKBaseMapper<DataSourcePO> {
    /**
     * 筛选器分页功能，查询数据源
     *
     * @param page  分页对象
     * @param query query对象
     * @return 查询结果
     */
    Page<DataSourceDTO> filter(Page<DataSourceDTO> page, @Param("query") DataSourcePageDTO query);
}
