package com.fisk.datamodel.dto.tablehistory;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author JianWenYang
 */
@Data
public class TableHistoryDTO {
    /**
     * 发布表id
     */
    public int tableId;
    /**
     * 发布表类型 0：维度表 1：事实表。。。
     */
    public int tableType;
    /**
     * 发布备注
     */
    public String  remark;
    /**
     * 创建时间
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    public LocalDateTime createTime;
}
