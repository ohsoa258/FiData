package com.fisk.dataaccess.dto.app;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author Lock
 * @version 2.8
 * @description
 * @date 2022/8/11 10:52
 */
@Data
public class DbNameDTO {

    @ApiModelProperty(value = "id")
    private long id;

    @ApiModelProperty(value = "数据库名称")
    private String dbName;
}
