package com.fisk.dataaccess.dto.table;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author Lock
 */
@Data
public class TableHistoryDTO {
    /**
     * 发布表id
     */
    public Integer tableId;
    /**
     * 发布表类型 0：RestfulAPI 1：非实时表  2:  api
     */
    public Integer tableType;
    /**
     * 发布备注
     */
    public String remark;
    /**
     * 创建时间
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    public LocalDateTime createTime;
    /**
     * 是否开启同步
     */
    public boolean openTransmission;
}
