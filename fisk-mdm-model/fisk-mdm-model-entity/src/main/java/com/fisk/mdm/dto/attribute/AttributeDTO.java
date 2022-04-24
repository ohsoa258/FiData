package com.fisk.mdm.dto.attribute;

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
    @Length(min = 0, max = 200, message = "长度最多50")
    private String desc;


    private String columnName;

    /**
     * 数据类型
     */
    @ApiModelProperty(value = "数据类型")
    @NotNull()
    private Integer dataType;

    /**
     * 数据类型长度
     */
    @ApiModelProperty(value = "数据类型长度")
    @NotNull()
    private Integer dataTypeLength;

    /**
     * 数据类型小数点长度
     */
    @ApiModelProperty(value = "数据类型小数点长度")
    private Integer dataTypeDecimalLength;

    /**
     * 数据格式id
     */
    @ApiModelProperty(value = "数据格式id")
    private Integer dataTypeFormatId;

    /**
     * 展示宽度
     */
    @ApiModelProperty(value = "展示宽度")
    @NotNull()
    private Integer displayWidth;

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
    private Integer sortWieght;

    /**
     *状态： 0：待新增 ，1：待修改 ， 2：已提交
     */
    @ApiModelProperty(value = "状态")
    private Integer status;

    /**
     * 提交状态：0：提交失败 1：提交成功
     */
    @ApiModelProperty(value = "提交状态")
    private Integer syncStatus;

    /**
     * 提交失败描述
     */
    @ApiModelProperty(value = "提交失败描述")
    @Length(min = 0, max = 200, message = "长度最多200")
    private String errorMsg;

    /**
     * 是否开启属性日志 0：false 1:true
     */
    @ApiModelProperty(value = "是否开启属性日志")
    @NotNull()
    private Boolean enableAttributeLog;

    /**
     * 是否只读 0：false 1:true
     */
    @ApiModelProperty(value = "是否只读")
    @NotNull()
    private Boolean enableReadonly;

    /**
     * 是否必填 0：false 1:true
     */
    @ApiModelProperty(value = "是否必填")
    @NotNull()
    private Boolean enableRequired;

    /**
     * 类型：
     * 0:code 1:name 2:业务字段
     */
    @ApiModelProperty(value = "类型")
    private Integer mdmType;

    /**
     * 数据域id（相当于外键）
     */
    private Integer domainId;

}
