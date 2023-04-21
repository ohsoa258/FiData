package com.fisk.dataaccess.dto.modelpublish;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * @author yoyo
 */
@Data
public class ModelPublishStatusDTO {
    /**
     * api_id
     */
    @ApiModelProperty(value = "api_id")
    public Long apiId;
    /**
     * 物理表id
     */
    @ApiModelProperty(value = "物理表id")
    @NotNull
    public long tableId;
    /**
     * 发布状态 1: 发布成功  2: 发布失败
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