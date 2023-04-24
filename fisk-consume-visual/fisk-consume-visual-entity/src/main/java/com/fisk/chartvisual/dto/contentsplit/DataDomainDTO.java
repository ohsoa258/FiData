package com.fisk.chartvisual.dto.contentsplit;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author gy
 */
@Data
public class DataDomainDTO {
    @ApiModelProperty(value = "表名")
    public String tableName;
    @ApiModelProperty(value = "表详细信息")
    public String tableDetails;

    @ApiModelProperty(value = "列名称")
    public String columnName;

    @ApiModelProperty(value = "列详细信息")
    public String columnDetails;
}
