package com.fisk.datagovernance.dto.dataquality.datacheck;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fisk.datagovernance.enums.dataquality.SourceTypeEnum;
import com.fisk.datagovernance.vo.dataquality.datacheck.DataCheckVO;
import io.swagger.annotations.ApiModelProperty;

/**
 * @author dick
 * @version 1.0
 * @description 数据校验查询DTO
 * @date 2022/3/24 13:21
 */
public class DataCheckQueryDTO {
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
     * 数据源类型
     */
    @ApiModelProperty(value = "数据源类型")
    public SourceTypeEnum datasourceType;

    /**
     * 实际表名称/表Id
     */
    @ApiModelProperty(value = "实际表名称/表Id")
    public String useTableName;

    /**
     * 分页对象
     */
    @ApiModelProperty(value = "分页对象")
    public Page<DataCheckVO> page;
}
