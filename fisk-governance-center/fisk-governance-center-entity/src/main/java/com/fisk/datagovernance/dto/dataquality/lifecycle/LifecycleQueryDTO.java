package com.fisk.datagovernance.dto.dataquality.lifecycle;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fisk.datagovernance.vo.dataquality.lifecycle.LifecycleVO;
import io.swagger.annotations.ApiModelProperty;

/**
 * @author dick
 * @version 1.0
 * @description TDDD
 * @date 2022/3/24 13:59
 */
public class LifecycleQueryDTO {
    /**
     * 搜索条件
     */
    @ApiModelProperty(value = "搜索条件")
    public String keyword;

    /**
     * 表名称
     */
    @ApiModelProperty(value = "表名称")
    public String tableName;

    /**
     * 分页对象
     */
    @ApiModelProperty(value = "分页对象")
    public Page<LifecycleVO> page;
}
