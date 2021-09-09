package com.fisk.task.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fisk.common.entity.BasePO;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
@TableName("tb_msg_log")
public class TaskDwDimPO extends BasePO {
public String areaBusinessBame;
public String tableName;
public String sqlContent;
public String storedProcedureName;

}
