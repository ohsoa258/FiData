package com.fisk.datafactory.dto.components;

import lombok.Data;

import java.util.List;

/**
 * @author Lock
 */
@Data
public class ChannelDataChildDTO {
    /**
     * 表id
     */
    public long id;
    /**
     * 表名称
     */
    public String tableName;

    /**
     * 此表被用到的管道,组,任务信息
     */
    public List<TableUsageDTO> tableUsages;
}
