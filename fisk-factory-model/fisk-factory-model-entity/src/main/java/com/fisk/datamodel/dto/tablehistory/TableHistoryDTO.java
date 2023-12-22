package com.fisk.datamodel.dto.tablehistory;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fisk.task.dto.DwLogResultDTO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Date;

/**
 * @author JianWenYang
 */
@Data
public class TableHistoryDTO {
    /**
     * 发布表id
     */
    @ApiModelProperty(value = "id")
    public int tableId;

    /**
     * 发布表类型 0：维度表 1：事实表。。。
     */
    @ApiModelProperty(value = "发布表类型 0：维度表 1：事实表。。。")
    public int tableType;

    /**
     * 发布备注
     */
    @ApiModelProperty(value = "发布备注")
    public String remark;

    /**
     * 创建时间
     */
    @ApiModelProperty(value = "创建时间")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    public LocalDateTime createTime;

    /**
     * 是否开启同步
     */
    @ApiModelProperty(value = "是否开启同步")
    public boolean openTransmission;

    /**
     * 本次同步详情
     */
    public DwLogResultDTO dto;
}
