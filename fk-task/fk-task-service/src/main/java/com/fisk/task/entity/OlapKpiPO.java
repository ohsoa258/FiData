package com.fisk.task.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fisk.common.entity.BasePO;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 创建模型维度PO
 * @author JinXingWang
 */
@EqualsAndHashCode(callSuper = true)
@Data
@TableName("tb_task_olap_kpi")
public class OlapKpiPO extends BasePO {
    public long businessAreaId;
    public String kpiTableName;
    public String createKpiTableSql;
    public String selectKpiDataSql;
}
