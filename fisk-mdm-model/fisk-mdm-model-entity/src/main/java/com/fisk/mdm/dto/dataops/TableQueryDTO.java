package com.fisk.mdm.dto.dataops;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author wangjian
 */
@Data
public class TableQueryDTO {

    @ApiModelProperty(value = "mdm表名称")
    public String mdmTableName;

    @ApiModelProperty(value = "Id")
    public Integer id;

    @ApiModelProperty(value = "模型Id")
    public Integer modelId;

}
