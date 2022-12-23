package com.fisk.dataservice.dto.datasource;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author JianWenYang
 */
@Data
public class DataSourceConfigInfoDTO {

    /**
     * id
     */
    @ApiModelProperty(value = "id")
    public int id;

    /**
     * 连接名称
     */
    @ApiModelProperty(value = "连接名称")
    public String name;

}
