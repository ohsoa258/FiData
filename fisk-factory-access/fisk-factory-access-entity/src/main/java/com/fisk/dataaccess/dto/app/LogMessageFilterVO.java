package com.fisk.dataaccess.dto.app;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author Lock
 * @version 1.3
 * @description
 * @date 2022/5/23 16:16
 */
@Data
public class LogMessageFilterVO {

    @ApiModelProperty(value = "应用id")
    public Long appId;
    @ApiModelProperty(value = "应用类型")
    public int appType;
    @ApiModelProperty(value = "api_id")
    public Long apiId;
    @ApiModelProperty(value = "api名称")
    public String apiName;
    @ApiModelProperty(value = "表id")
    public Long tableId;
    @ApiModelProperty(value = "表名称")
    public String tableName;

}
