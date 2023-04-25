package com.fisk.mdm.dto.accessmodel;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author wangjian
 */
@Data
public class AccessPublishStatusDTO {
    /**
     * entityId
     */
    @ApiModelProperty(value = "id")
    public int id;
    /**
     * 发布状态
     */
    @ApiModelProperty(value = "发布状态")
    public int status;
}
