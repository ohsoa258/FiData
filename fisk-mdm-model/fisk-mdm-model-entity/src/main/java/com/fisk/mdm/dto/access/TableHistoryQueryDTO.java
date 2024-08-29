package com.fisk.mdm.dto.access;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author wangjian
 */
@Data
public class TableHistoryQueryDTO {
    @ApiModelProperty(value = "表Id")
    public int tableId;
    /**
     * 表类型
     */
    @ApiModelProperty(value = "表类型")
    public int tableType;

    /**
     * 发布时间
     */
    @ApiModelProperty(value = "发布时间")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    public LocalDateTime publishTime;
}
