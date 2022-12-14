package com.fisk.datagovernance.dto.dataquality.datasource;

import com.fisk.common.core.enums.fidatadatasource.LevelTypeEnum;
import com.fisk.datagovernance.enums.dataquality.SourceTypeEnum;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author dick
 * @version 1.0
 * @description 表信息
 * @date 2022/12/2 16:32
 */
@Data
public class QueryTableRuleDTO
{
    @ApiModelProperty(value = "标识ID")
    public String id;

    @ApiModelProperty(value = "标识名称")
    public String name;

    @ApiModelProperty(value = "数据源Id")
    public int sourceId;

    @ApiModelProperty(value = "数据源类型")
    public SourceTypeEnum sourceType;

    @ApiModelProperty(value = "业务类型，表和视图维度设置")
    public int tableBusinessType;

    @ApiModelProperty(value = "层级类型")
    public LevelTypeEnum tableType;
}
