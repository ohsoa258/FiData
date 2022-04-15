package com.fisk.datagovernance.dto.dataquality.lifecycle;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
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
     * 表名称
     */
    @ApiModelProperty(value = "表名称")
    public String tableName;

    /**
     * 数据源id 不直接使用数据源id是考虑在数据资产页面打开数据质量时没有数据源id
     */
//    @ApiModelProperty(value = "数据源id")
//    public int datasourceId;

    /**
     * IP
     */
    @ApiModelProperty(value = "IP")
    public String conIp;

    /**
     * 数据库名称
     */
    @ApiModelProperty(value = "数据库名称")
    public String conDbname;

    /**
     * 分页对象
     */
    @ApiModelProperty(value = "分页对象")
    public Page<LifecycleVO> page;
}
