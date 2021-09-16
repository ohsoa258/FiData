package com.fisk.task.entity;

import com.fisk.common.entity.BasePO;
import com.fisk.task.enums.OlapTableEnum;

/**
 * @author JinXingWang
 */
public class OlapPO extends BasePO {
    public long businessAreaId;
    public String tableName;
    public String createTableSql;
    public String selectDataSql;
    public OlapTableEnum type;
}
