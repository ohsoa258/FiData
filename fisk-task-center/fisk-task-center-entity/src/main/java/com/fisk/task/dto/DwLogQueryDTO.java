package com.fisk.task.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fisk.task.enums.OlapTableEnum;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Date;

@Data
public class DwLogQueryDTO {

    /**
     * 表id
     */
    @ApiModelProperty(value = "表id")
    private Integer tblId;

    /**
     * 表名
     */
    @ApiModelProperty(value = "表名")
    private String tblName;

    /**
     * 表类别
     */
    @ApiModelProperty(value = "表类别")
    private OlapTableEnum tblType;

    /**
     * 发布时间
     */
    @ApiModelProperty(value = "发布时间")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime publishTime;

}
