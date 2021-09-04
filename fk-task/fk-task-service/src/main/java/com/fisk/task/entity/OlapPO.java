package com.fisk.task.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fisk.common.entity.BasePO;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 创建模型PO
 * @author JinXingWang
 */
@EqualsAndHashCode(callSuper = true)
@Data
@TableName("tb_task_olap")
public class OlapPO extends BasePO {
    public long businessAreaId;
    public String dimensionTableName;
    public String createDimensionTableSql;
    public String kpiTableName;
    public String createKpiTableSql;
    public String selectKpiDataSql;
}
