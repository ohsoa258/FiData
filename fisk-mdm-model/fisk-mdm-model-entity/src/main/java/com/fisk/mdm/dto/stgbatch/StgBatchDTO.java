package com.fisk.mdm.dto.stgbatch;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author JianWenYang
 */
@Data
public class StgBatchDTO {

    @ApiModelProperty(value = "实体id")
    private Integer entityId;

    @ApiModelProperty(value = "版本id")
    private Integer versionId;

    @ApiModelProperty(value = "批次编码")
    private String batchCode;

    @ApiModelProperty(value = "合计计数")
    private Integer totalCount;

    @ApiModelProperty(value = "错误计数")
    private Integer errorCount;

    @ApiModelProperty(value = "状况")
    private Integer status;

    @ApiModelProperty(value = "添加计数")
    private Integer addCount;
    @ApiModelProperty(value = "更新计数")
    private Integer updateCount;

}
