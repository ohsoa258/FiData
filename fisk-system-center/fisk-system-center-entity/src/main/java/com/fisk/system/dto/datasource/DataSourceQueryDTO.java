package com.fisk.system.dto.datasource;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fisk.common.service.pageFilter.dto.FilterQueryDTO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author dick
 * @version v1.0
 * @description 数据源查询 DTO
 * @date 2022/6/13 14:51
 */
@Data
public class DataSourceQueryDTO {
    /**
     * 筛选器对象
     */
    @ApiModelProperty(value = "筛选器对象")
    public List<FilterQueryDTO> dto;

    /**
     * 分页对象
     */
    @ApiModelProperty(value = "分页对象")
    public Page<DataSourceDTO> page;
}
