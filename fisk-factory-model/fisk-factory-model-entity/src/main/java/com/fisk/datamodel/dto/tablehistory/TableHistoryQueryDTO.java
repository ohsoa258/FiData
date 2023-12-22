package com.fisk.datamodel.dto.tablehistory;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

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
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    public Date publishTime;

}
