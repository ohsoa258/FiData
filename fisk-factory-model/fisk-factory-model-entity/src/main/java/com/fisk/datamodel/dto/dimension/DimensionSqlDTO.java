package com.fisk.datamodel.dto.dimension;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author JianWenYang
 */
@Data
public class DimensionSqlDTO {
    @ApiModelProperty(value = "Id")
    public long id;
    // public Integer appId;
    @ApiModelProperty(value = "数据源Id")
    public Integer dataSourceId;

    @ApiModelProperty(value = "sql脚本")
    public String sqlScript;
}
