package com.fisk.dataaccess.dto.datamodel;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author Lock
 */
@Data
public class DataModelDTO {

    @ApiModelProperty(value = "应用id")
    public long appId;

    @ApiModelProperty(value = "应用名称")
    public String appName;
//    public
    
}
