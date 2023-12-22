package com.fisk.datamodel.dto.tablehistory;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.util.Date;

/**
 * @author JianWenYang
 */
@Data
public class TableHistoryQueryDTO {

    /**
     * 表Id
     */
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
