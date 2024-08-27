package com.fisk.datagovernance.dto.dataquality.datacheck;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author dick
 * @version 1.0
 * @description 数据校验日志评语DTO
 * @date 2024/8/26 10:07
 */
@Data
public class DataCheckLogCommentDTO {
    /**
     * 主键id
     */
    @ApiModelProperty(value = "主键id")
    public int id;

    /**
     * 质量分析
     */
    @ApiModelProperty(value = "质量分析")
    public String qualityAnalysis;
}
