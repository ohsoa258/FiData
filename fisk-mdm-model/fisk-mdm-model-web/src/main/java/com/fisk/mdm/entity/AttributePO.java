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
     *状态： 0：新增 ，1：修改 ，2:已提交 3：删除
     */
    private AttributeStatusEnum status;

    /**
     * 提交状态：0：提交失败 1：提交成功
     */
    private AttributeSyncStatusEnum syncStatus;

    /**
     * 提交失败描述
     */
    private String errorMsg;

    /**
     * 数据类型
     */
    private DataTypeEnum dataType;

    /**
     * 数据类型长度
     */
    @TableField(insertStrategy = FieldStrategy.IGNORED,updateStrategy = FieldStrategy.IGNORED)
    private Integer dataTypeLength;

    /**
     * 数据类型小数点长度
     */
    @TableField(insertStrategy = FieldStrategy.IGNORED,updateStrategy = FieldStrategy.IGNORED)
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
}
