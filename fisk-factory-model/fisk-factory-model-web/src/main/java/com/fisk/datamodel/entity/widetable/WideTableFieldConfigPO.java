package com.fisk.datamodel.entity.widetable;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fisk.common.core.baseObject.entity.BasePO;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author JianWenYang
 */
@Data
@TableName("tb_wide_table_field_config")
@EqualsAndHashCode(callSuper = true)
public class WideTableFieldConfigPO extends BasePO {
    /**
     * 宽表配置主表id
     */
    public Integer wideTableId;
    /**
     * 表名称
     */
    public String tableName;
    /**
     * 表类型：0维度 1事实
     */
    public Integer tableType;
    /**
     * 字段名称
     */
    public String fieldName;
    /**
     * 字段类型
     */
    public String fieldType;
    /**
     * 字段类型长度
     */
    public Integer fieldLength;
    /**
     * 字段别名
     */
    public String alias;

}
