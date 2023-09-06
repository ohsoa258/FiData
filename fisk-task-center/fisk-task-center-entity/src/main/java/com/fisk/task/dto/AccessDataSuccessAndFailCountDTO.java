package com.fisk.task.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class AccessDataSuccessAndFailCountDTO {

    /**
     * 成功计数
     */
    @ApiModelProperty(value = "成功计数")
    private Integer successCount;

    /**
     * 失败计数
     */
    @ApiModelProperty(value = "失败计数")
    private Integer failCount;

}
