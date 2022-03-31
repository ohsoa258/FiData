package com.fisk.task.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fisk.common.core.baseObject.entity.BasePO;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
@TableName("tb_task_dw_dim")
public class TaskDwDimPO extends BasePO {
public String areaBusinessName;
public String tableName;
public String sqlContent;
public String storedProcedureName;

}
