package com.fisk.datagovernance.dto.dataquality.lifecycle;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fisk.datagovernance.enums.dataquality.SourceTypeEnum;
import com.fisk.datagovernance.vo.dataquality.lifecycle.LifecycleVO;
import io.swagger.annotations.ApiModelProperty;

/**
 * @author dick
 * @version 1.0
 * @description 生命周期查询DTO
 * @date 2022/3/24 13:59
 */
public class LifecycleQueryDTO {
    /**
     * 搜索条件
     */
    @ApiModelProperty(value = "搜索条件")
    public String keyword;

    /**
     * 数据源表主键id
     */
    @ApiModelProperty(value = "数据源表主键id")
    public int datasourceId;

    /**
     * 表名称/表Id
     */
    @ApiModelProperty(value = "表名称/表Id")
    public String tableUnique;

    /**
     * 分页对象
     */
    @ApiModelProperty(value = "分页对象")
    public Page<LifecycleVO> page;
}
