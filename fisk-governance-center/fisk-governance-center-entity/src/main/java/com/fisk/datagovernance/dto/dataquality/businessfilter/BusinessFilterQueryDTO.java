package com.fisk.datagovernance.dto.dataquality.businessfilter;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fisk.datagovernance.vo.dataquality.businessfilter.BusinessFilterVO;
import io.swagger.annotations.ApiModelProperty;

/**
 * @author dick
 * @version 1.0
 * @description 业务清洗查询DTO
 * @date 2022/3/24 13:47
 */
public class BusinessFilterQueryDTO {
    /**
     * 搜索条件
     */
    @ApiModelProperty(value = "搜索条件")
    public String keyword;

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
     * 表名称
     */
    @ApiModelProperty(value = "表名称")
    public String tableName;

    /**
     * 分页对象
     */
    @ApiModelProperty(value = "分页对象")
    public Page<BusinessFilterVO> page;
}
