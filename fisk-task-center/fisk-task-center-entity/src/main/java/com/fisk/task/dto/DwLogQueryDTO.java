package com.fisk.task.dto;

import com.fisk.task.enums.OlapTableEnum;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

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
    private Date publishTime;

}
