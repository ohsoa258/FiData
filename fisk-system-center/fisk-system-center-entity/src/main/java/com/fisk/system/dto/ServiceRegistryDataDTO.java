package com.fisk.system.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author JianWenYang
 */
@Data
public class ServiceRegistryDataDTO {
    @ApiModelProperty(value = "Id")
    public long id;
    @ApiModelProperty(value = "服务中文名")
    public String serveCnName;
}
