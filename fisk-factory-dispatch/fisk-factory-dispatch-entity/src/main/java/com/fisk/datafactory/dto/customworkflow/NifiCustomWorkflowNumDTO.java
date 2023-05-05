package com.fisk.datafactory.dto.customworkflow;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author Lock
 */
@Data
public class NifiCustomWorkflowNumDTO {

    /**
     * 发布成功
     */
    @ApiModelProperty(value = "发布成功")
    public int success;
    /**
     * 发布失败
     */
    @ApiModelProperty(value = "发布失败")
    public int failure;
    /**
     * 正在发布,运行
     */
    @ApiModelProperty(value = "正在发布,运行")
    public int running;
    /**
     * 未发布,未运行
     */
    @ApiModelProperty(value = "未发布,未运行")
    public int notRun;
}
