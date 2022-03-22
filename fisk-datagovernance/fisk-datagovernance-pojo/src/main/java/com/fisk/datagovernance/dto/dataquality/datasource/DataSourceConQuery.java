package com.fisk.datagovernance.dto.dataquality.datasource;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fisk.datagovernance.vo.dataquality.datasource.DataSourceConVO;
import io.swagger.annotations.ApiModelProperty;

/**
 * @author dick
 * @version v1.0
 * @description 数据源 查询条件
 * @date 2022/1/6 14:51
 */
public class DataSourceConQuery {
    /**
     * 关键字
     */
    @ApiModelProperty(value = "关键字")
    public String keyword;

    /**
     * 分页对象
     */
    @ApiModelProperty(value = "分页对象")
    public Page<DataSourceConVO> page;
}
