package com.fisk.dataservice.dto.datasource;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author JianWenYang
 */
@Data
public class DataSourceColumnQueryDTO {

    @ApiModelProperty(value = "数据库名")
    public String dbName;

    @ApiModelProperty(value = "表名")
    public String tableName;

}
