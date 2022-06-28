package com.fisk.datagovernance.dto.dataquality.businessfilter;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fisk.datagovernance.enums.DataSourceTypeEnum;
import com.fisk.datagovernance.enums.dataquality.SourceTypeEnum;
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
     * 数据源表主键id
     */
    @ApiModelProperty(value = "数据源表主键id")
    public int datasourceId;

    /**
     * 实际表名称/表Id
     */
    @ApiModelProperty(value = "实际表名称/表Id")
    public String useTableName;

    /**
     * 分页对象
     */
    @ApiModelProperty(value = "分页对象")
    public Page<BusinessFilterVO> page;
}
