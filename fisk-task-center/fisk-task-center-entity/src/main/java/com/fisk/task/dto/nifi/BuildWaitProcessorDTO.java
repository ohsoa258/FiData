package com.fisk.task.dto.nifi;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author cfk
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class BuildWaitProcessorDTO  extends BaseProcessorDTO {

    @ApiModelProperty(value = "释放信号识别器")
    public String releaseSignalIdentifier;
    @ApiModelProperty(value = "目标信号计数")
    public String targetSignalCount;
    @ApiModelProperty(value = "过期时间")
    public String expirationDuration;
    @ApiModelProperty(value = "分布式缓存服务")
    public String distributedCacheService;
}
