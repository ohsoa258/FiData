package com.fisk.dataservice.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fisk.common.core.baseObject.entity.BasePO;
import lombok.Data;

/**
 * @author JianWenYang
 */
@Data
@TableName("tb_table_service")
public class TableServicePO extends BasePO {

    /**
     * 表名
     */
    public String tableName;

    /**
     * 显示名称
     */
    public String displayName;

    /**
     * 表描述
     */
    public String tableDes;

    /**
     * sql脚本
     */
    public String sqlScript;

    /**
     * 目标数据源id
     */
    public Integer targetDbId;

    /**
     * 表添加方式: 1创建新表 2选择现有表
     */
    public Integer addType;

    /**
     * 目标表名称
     */
    public String targetTable;

    /**
     * 来源库id
     */
    public Integer sourceDbId;

    /**
     * 发布状态: 0: 未发布  1: 发布成功  2: 发布失败
     */
    public Integer publish;

}
