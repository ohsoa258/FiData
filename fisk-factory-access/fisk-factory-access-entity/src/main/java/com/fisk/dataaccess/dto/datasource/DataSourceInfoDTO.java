package com.fisk.dataaccess.dto.datasource;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author JianWenYang
 */
@Data
public class DataSourceInfoDTO {

    @ApiModelProperty(value = "名称")
    public String name;

    @ApiModelProperty(value = "id")
    public long id;

}
