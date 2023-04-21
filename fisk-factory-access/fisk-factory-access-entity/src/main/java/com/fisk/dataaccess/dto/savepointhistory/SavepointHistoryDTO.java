package com.fisk.dataaccess.dto.savepointhistory;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author JianWenYang
 */
@Data
public class SavepointHistoryDTO {

    @ApiModelProperty(value = "id")
    public Long id;

    @ApiModelProperty(value = "目标表ID")
    public Long tableAccessId;

    /**
     * 检查点路径
     */
    @ApiModelProperty(value = "检查点路径")
    public String savepointPath;

    /**
     * 检查点时间
     */
    @ApiModelProperty(value = "检查点时间")
    public LocalDateTime savepointDate;

}
