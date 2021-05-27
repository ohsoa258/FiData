package com.fisk.chartvisual.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fisk.chartvisual.dto.DataSourceConQuery;
import com.fisk.chartvisual.entity.DataSourceConPO;
import com.fisk.chartvisual.vo.DataSourceConVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 数据源连接mapper
 *
 * @author gy
 */
@Mapper
public interface DataSourceConMapper extends BaseMapper<DataSourceConPO> {

    /**
     * 获取权限下所有数据源连接
     *
     * @return 查询结果
     */
    Page<DataSourceConVO> listDataSourceConByUserId(Page<DataSourceConVO> page, @Param("query") DataSourceConQuery query);
}
