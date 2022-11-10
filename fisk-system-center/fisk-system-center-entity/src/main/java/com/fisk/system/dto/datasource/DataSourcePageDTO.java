package com.fisk.system.dto.datasource;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author dick
 * @version 1.0
 * @description 数据源查询 分页对象
 * @date 2022/10/24 10:27
 */
@Data
public class DataSourcePageDTO {
    /**
     * 条件
     */
    @ApiModelProperty(value = "条件")
    public String where;

    /**
     * 分页
     */
    @ApiModelProperty(value = "分页")
    public Page<DataSourceDTO> page;
}
