package com.fisk.task.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fisk.common.entity.BasePO;
import com.fisk.task.enums.OlapTableEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author JinXingWang
 */
@EqualsAndHashCode(callSuper = true)
@Data
@TableName("tb_task_olap")
public class OlapPO extends BasePO {
    public long businessAreaId;
    public String tableName;
    public String createTableSql;
    public String selectDataSql;
    public OlapTableEnum type;
}
