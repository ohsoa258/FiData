package com.fisk.mdm.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fisk.common.core.baseObject.entity.BasePO;
import com.fisk.mdm.enums.DataTypeEnum;
import com.fisk.mdm.enums.MdmTypeEnum;
import com.fisk.mdm.enums.WhetherTypeEnum;
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
    private String cloumnName;

    /**
     * 数据类型
     */
    private DataTypeEnum dataType;

    /**
     * 数据类型长度
     */
    private int dataTypeLength;

    /**
     * 数据格式id
     */
    private int dataTypeFormatId;

    /**
     * 展示宽度
     */
    private int displayWidth;

    /**
     * 表单框提示内容
     */
    private String formPrompContent;

    /**
     * 排序序号
     */
    private int sortWieght;

    /**
     * 是否开启属性日志 0：false 1:true
     */
    private WhetherTypeEnum enableAttribuleLog;

    /**
     * 是否只读 0：false 1:true
     */
    private WhetherTypeEnum enableReadonaly;

    /**
     * 是否必填 0：false 1:true
     */
    private WhetherTypeEnum enableRequired;

    /**
     * 类型：
     * 0:code 1:name 2:业务字段
     */
    private MdmTypeEnum mdmType;

    /**
     * 数据域id（相当于外键）
     */
    private int domainId;
}
