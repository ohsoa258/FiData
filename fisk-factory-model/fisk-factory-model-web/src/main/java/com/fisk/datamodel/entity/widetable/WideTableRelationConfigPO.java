package com.fisk.datamodel.entity.widetable;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fisk.common.core.baseObject.entity.BasePO;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author JianWenYang
 */
@Data
@TableName("tb_wide_table_relation_config")
@EqualsAndHashCode(callSuper = true)
public class WideTableRelationConfigPO extends BasePO {
    /**
     * 宽表配置主表id
     */
    public Integer wideTableId;
    /**
     * 来源表名称
     */
    public String sourceTable;
    /**
     * 来源表关联字段
     */
    public String sourceColumn;
    /**
     * 关联方式
     */
    public String joinType;
    /**
     * 目标表名称
     */
    public String targetTable;
    /**
     * 目标表关联字段
     */
    public String targetColumn;

}
