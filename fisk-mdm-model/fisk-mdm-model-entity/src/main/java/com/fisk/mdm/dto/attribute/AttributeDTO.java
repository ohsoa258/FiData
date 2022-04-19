package com.fisk.mdm.dto.attribute;

import com.fisk.mdm.enums.*;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotNull;

/**
 * @author ChenYa
 */
@Data
public class AttributeDTO {

    /**
     * id
     */
    private Integer id;

    @ApiModelProperty(value = "实体id")
    @NotNull()
    private Integer entityId;

    @ApiModelProperty(value = "属性名称")
    @NotNull()
    @Length(min = 0, max = 50, message = "长度最多50")
    private String name;

    /**
     * 展示名称
     */
    @ApiModelProperty(value = "属性展示名称")
    @NotNull()
    @Length(min = 0, max = 50, message = "长度最多50")
    private String displayName;

    /**
     * 描述
     */
    @ApiModelProperty(value = "描述")
    @NotNull()
    @Length(min = 0, max = 200, message = "长度最多50")
    private String desc;

    /**
     * 描述
     */
    private String columnName;

    /**
     * 数据类型
     */
    @ApiModelProperty(value = "数据类型")
    @NotNull()
    private DataTypeEnum dataType;

    /**
     * 数据类型长度
     */
    @ApiModelProperty(value = "数据类型长度")
    @NotNull()
    private int dataTypeLength;

    /**
     * 数据格式id
     */
    @ApiModelProperty(value = "数据格式id")
    private int dataTypeFormatId;

    /**
     * 展示宽度
     */
    @ApiModelProperty(value = "展示宽度")
    @NotNull()
    private int displayWidth;

    /**
     * 表单框提示内容
     */
    @ApiModelProperty(value = "表单框提示内容")
    @NotNull()
    @Length(min = 0, max = 200, message = "长度最多200")
    private String formPrompContent;

    /**
     * 排序序号
     */
    @ApiModelProperty(value = "排序序号")
    @NotNull()
    private int sortWieght;

    /**
     *状态： 0：待新增 ，1：待修改 ， 2：已提交
     */
    @ApiModelProperty(value = "状态")
    @NotNull()
    private AttributeStatusEnum status;

    /**
     * 提交状态：0：提交失败 1：提交成功
     */
    @ApiModelProperty(value = "提交状态")
    @NotNull()
    private AttributeSyncStatusEnum syncStatus;

    /**
     * 提交失败描述
     */
    @ApiModelProperty(value = "提交失败描述")
    @NotNull()
    @Length(min = 0, max = 200, message = "长度最多200")
    private String errorMsg;

    /**
     * 是否开启属性日志 0：false 1:true
     */
    @ApiModelProperty(value = "是否开启属性日志")
    @NotNull()
    private WhetherTypeEnum enableAttributeLog;

    /**
     * 是否只读 0：false 1:true
     */
    @ApiModelProperty(value = "是否只读")
    @NotNull()
    private WhetherTypeEnum enableReadonly;

    /**
     * 是否必填 0：false 1:true
     */
    @ApiModelProperty(value = "是否必填")
    @NotNull()
    private WhetherTypeEnum enableRequired;

    /**
     * 类型：
     * 0:code 1:name 2:业务字段
     */
    @ApiModelProperty(value = "类型")
    @NotNull()
    private MdmTypeEnum mdmType;

    /**
     * 数据域id（相当于外键）
     */
    private Integer domainId;

}
