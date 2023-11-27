package com.fisk.datamanagement.dto.standards;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @Author: wangjian
 * @Date: 2023-11-21
 * @Description:
 */
@Data
public class ColumnQueryDTO {
    @ApiModelProperty(value = "数据库id")
    public Integer dbId;
    @ApiModelProperty(value = "表名称")
    public String tableName;
}
