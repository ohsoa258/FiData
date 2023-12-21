package com.fisk.dataaccess.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @Author: wangjian
 * @Date: 2023-12-21
 * @Description:
 */
@Data
public class TableDbNameAndNameVO {
    @ApiModelProperty(value = "库名称")
    private String dbName;
    @ApiModelProperty(value = "表名称")
    private String tableName;
}
