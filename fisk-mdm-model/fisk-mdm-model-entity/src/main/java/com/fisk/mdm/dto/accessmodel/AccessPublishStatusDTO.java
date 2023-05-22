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
    public int publish;

    /**
     * 发布错误信息
     */
    @ApiModelProperty(value = "发布错误信息")
    public String publishErrorMsg;
    /**
     * 关联发布日志标识
     */
    @ApiModelProperty(value = "关联发布日志标识")
    public String subRunId;
    /**
     * 发布历史id
     */
    @ApiModelProperty(value = "发布历史id")
    public Long tableHistoryId;
}
