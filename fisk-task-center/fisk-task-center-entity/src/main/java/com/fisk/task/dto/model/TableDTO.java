package com.fisk.task.dto.model;

import com.fisk.task.dto.MQBaseDTO;
import lombok.Data;

/**
 * @Author: wangjian
 * @Date: 2024-01-16
 * @Description:
 */
@Data
public class TableDTO  extends MQBaseDTO {
    public String mdmTableName;
    public String stgTableName;
    public String logTableName;
    public String viwTableName;
}
