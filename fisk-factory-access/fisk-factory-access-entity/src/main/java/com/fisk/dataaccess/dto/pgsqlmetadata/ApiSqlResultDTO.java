package com.fisk.dataaccess.dto.pgsqlmetadata;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author Lock
 * @version 1.3
 * @description api推送数据执行的ql方法的出参
 * @date 2022/5/27 15:36
 */
@Data
public class ApiSqlResultDTO {

    @ApiModelProperty(value = "本次同步的条数")
    private int count;
    @ApiModelProperty(value = "本次同步的表名")
    private String tableName;
    @ApiModelProperty(value = "同步的日志消息")
    private String msg;
}
