package com.fisk.datamanagement.dto.dataquality;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author JianWenYang
 */
@Data
public class DataQualityDTO {

    /**
     * 实例名
     */
    @ApiModelProperty(value = "实例名")
    public String instanceName;
    /**
     * 库名
     */
    @ApiModelProperty(value = "库名")
    public String dbName;
    /**
     * 表名
     */
    @ApiModelProperty(value = "表名")
    public String port;
    /**
     * 数据库类型
     */
    @ApiModelProperty(value = "数据库类型")
    public String rdbmsType;

}
