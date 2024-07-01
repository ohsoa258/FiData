package com.fisk.dataaccess.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fisk.common.core.baseObject.entity.BasePO;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author Lock
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("tb_table_fields")
public class TableFieldsPO extends BasePO {
    @TableId(value = "id", type = IdType.AUTO)
    public long id;
    /**
     * table_access（id）
     */
    public Long tableAccessId;

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
    public Long fieldLength;

    /**
     * 字段推送规则
     */
    public String fieldPushRule;

    /**
     * 字段推送示例
     */
    public String fieldPushExample;

    /**
     * 1是主键，0非主键
     */
    public Integer isPrimarykey;

    /**
     * 1是业务时间，0非业务时间
     */
    public int isBusinesstime;

    /**
     * 1：实时物理表的字段，0：非实时物理表的字段
     */
    public Integer isRealtime;

    /**
     * 1是时间戳，0非时间戳
     */
    public int isTimestamp;

    /**
     * 物理表字段显示名称
     */
    public String displayName;

    /**
     * 字段精度
     */
    public Integer fieldPrecision;

    /**
     * 是否是敏感字段 0否 1是
     */
    public Integer isSensitive;

    /**
     * 是否是分区字段 0否 1是
     */
    public Integer isPartitionKey;

    /**
     * 是否为空 0否 1是
     */
    public Integer isEmpty;

    /**
     * 源库名称
     */
    public String sourceDbName;

    /**
     * 源表名称
     */
    public String sourceTblName;

    /**
     * 数据分类：DataClassificationEnum
     * PUBLIC_DATA(1, "公开数据", "green"),
     * INTERNAL_DATA(2, "内部数据", "blue"),
     * MAX(3, "敏感数据", "orange"),
     * MIN(4, "高度敏感数据", "red"),
     */
    public Integer dataClassification;

    /**
     * 数据分级：DataLevelEnum
     * LEVEL1(1, "一级（一般数据）", "green"),
     * LEVEL2(2, "二级（重要数据）", "blue"),
     * LEVEL3(3, "三级（敏感数据）", "orange"),
     * LEVEL4(4, "四级（核心数据）", "red"),
     */
    public Integer dataLevel;

}
