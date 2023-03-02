package com.fisk.datafactory.dto.components;

import com.fisk.common.core.enums.fidatadatasource.DataSourceConfigEnum;
import com.fisk.common.core.enums.fidatadatasource.TableBusinessTypeEnum;
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
     * 表业务类型
     */
    public int tableBusinessType;
    /**
     * 数据源id
     * 1、dmp_dw
     * 2、dmp_ods
     */
    public int sourceId;
    /**
     * 此表被用到的管道,组,任务信息
     */
    public List<TableUsageDTO> tableUsages;
}
