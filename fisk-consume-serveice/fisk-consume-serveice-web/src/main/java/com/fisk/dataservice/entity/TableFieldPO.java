package com.fisk.dataservice.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fisk.common.core.baseObject.entity.BasePO;
import lombok.Data;

/**
 * @author JianWenYang
 */
@Data
@TableName("tb_table_field")
public class TableFieldPO extends BasePO {

    /**
     * 表服务id
     */
    public Integer tableServiceId;

    /**
     * 源字段
     */
    public String sourceFieldName;

    /**
     * 源字段类型
     */
    public String sourceFieldType;

    /**
     * 字段名称
     */
    public String fieldName;

    /**
     * 显示名称
     */
    public String displayName;

    /**
     * 字段描述
     */
    public String fieldDes;

    /**
     * 字段类型
     */
    public String fieldType;

    /**
     * 字段长度
     */
    public Integer fieldLength;

    /**
     * 字段精度
     */
    public Integer fieldPrecision;

    /**
     * 是否主键
     */
    public Boolean primaryKey;

}
