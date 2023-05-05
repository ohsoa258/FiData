package com.fisk.task.dto.nifi;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author cfk
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class BuildRedisConnectionPoolServiceDTO extends BaseProcessorDTO {

    @ApiModelProperty(value = "开启")
    public boolean enabled;
    @ApiModelProperty(value = "连接字符串")
    public String connectionString;

}
