package com.fisk.mdm.entity;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fisk.common.core.baseObject.entity.BasePO;
import com.fisk.mdm.enums.*;
import lombok.Data;

/**
 * @author WangYan
 * @date 2022/4/5 12:11
 */
@TableName("tb_attribute")
@Data
public class AttributePO extends BasePO {

    /**
     * 实体id
     */
    private Integer entityId;

    /**
     * 属性名称
     */
    private String name;

    /**
     * 属性名称(旧)
     */
    private String oldName;

    /**
     * 展示名称
     */
    private String displayName;

    /**
     * 描述
     */
    @TableField(value = "`desc`")
    private String desc;

    /**
     * 列名（底层表中通过编码规则创建出来的列名）
     */
    private String columnName;

    /**
     *状态： 0：新增 ，1：修改 ，2:发布 3：删除
     */
    private AttributeStatusEnum status;

    /**
     * 发布状态：0：发布失败 1：发布成功
     */
    private AttributeSyncStatusEnum syncStatus;

    /**
     * 发布失败描述
     */
    private String errorMsg;

    /**
     * 数据类型
     */
    private DataTypeEnum dataType;

    /**
     * 地图类型：0高德地图，1百度地图
     */
    private MapTypeEnum mapType;

    /**
     * 数据类型长度
     */
    private Integer dataTypeLength;

    /**
     * 数据类型小数点长度
     */
    private Integer dataTypeDecimalLength;

    /**
     * 数据格式id
     */
    private Integer dataTypeFormatId;

    /**
     * 展示宽度
     */
    private Integer displayWidth;
    /**
     * 数据规则
     */
    private DataRuleEnum dataRule;
    /**
     * 表单框提示内容
     */
    private String formPrompContent;

    /**
     * 排序序号
     */
    private Integer sortWieght;

    /**
     * 是否开启属性日志 0：false 1:true
     */
    private Integer enableAttributeLog;

    /**
     * 是否只读 0：false 1:true
     */
    private Integer enableReadonly;

    /**
     * 是否必填 0：false 1:true
     */
    private Integer enableRequired;

    /**
     * 类型：
     * 0:code 1:name 2:业务字段
     */
    private MdmTypeEnum mdmType;

    /**
     * 数据域id（相当于外键）
     */
    private Integer domainId;

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
