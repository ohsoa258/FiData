package com.fisk.dataservice.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fisk.common.core.baseObject.entity.BasePO;
import lombok.Data;

/**
 * @author dick
 * @version v1.0
 * @description 字段实体类
 * @date 2022/1/6 14:51
 */
@Data
@TableName("tb_field_config")
public class FieldConfigPO extends BasePO
{
    /**
     * API Id
     */
    public int apiId;

    /**
     * 字段名称
     */
    public String fieldName;

    /**
     * 字段类型
     */
    public String fieldType;

    /**
     * 字段排序
     */
    public int fieldSort;

    /**
     * 字段描述
     */
    public String fieldDesc;

    /**
     * 加密
     */
    public int encrypt;

    @TableField
    public Integer desensitization;

    /**
     * 数据分类：DataClassificationEnum
     * PUBLIC_DATA(1, "公共数据", "public"),
     * PERSONAL_DATA(2, "个人信息", "personal"),
     * ORGANIZED_DATA(3, "组织数据", "organized"),
     */
    public Integer dataClassification;
    /**
     * 数据分级：DataLevelEnum
     * LEVEL1(1, "一级（一般数据）", "green"),
     * LEVEL2(2, "二级（重要数据）", "blue"),
     * LEVEL4(4, "四级（核心数据）", "red"),
     */
    public Integer dataLevel;
}
