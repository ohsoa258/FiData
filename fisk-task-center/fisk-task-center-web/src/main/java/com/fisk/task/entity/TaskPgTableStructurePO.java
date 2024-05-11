package com.fisk.task.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fisk.common.core.baseObject.entity.BasePO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author JianWenYang
 */
@EqualsAndHashCode(callSuper = true)
@Data
@TableName("tb_task_pg_table_structure")
public class TaskPgTableStructurePO extends BasePO {
    /**
     * 版本号
     */
    public String version;

    /**
     * 表名
     */
    public String tableName;

    /**
     * 表id
     */
    public String tableId;

    /**
     * 字段名称
     */
    public String fieldName;

    /**
     * 字段id
     */
    public String fieldId;

    /**
     * 字段类型
     */
    public String fieldType;

    /**
     * 字段描述
     */
    public String fieldDes;

    /**
     * 0：数据接入 1：事实表 2：维度表
     */
    public int tableType;

    /**
     * 是否为业务主键
     */
    public boolean primaryKey;
    /**
     * 有效版本
     */
    public int validVersion;

}
