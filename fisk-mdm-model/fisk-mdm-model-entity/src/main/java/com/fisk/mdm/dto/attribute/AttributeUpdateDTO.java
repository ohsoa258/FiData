package com.fisk.mdm.dto.attribute;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * @author ChenYa
 */
@Data
public class AttributeUpdateDTO {
    @NotNull
    @ApiModelProperty(value = "id",required = true)
    private Integer id;

    @ApiModelProperty(value = "实体id")
    private Integer entityId;

    @ApiModelProperty(value = "属性名称")
    @Length(min = 0, max = 50, message = "长度最多50")
    private String name;

    @ApiModelProperty(value = "旧名称")
    @Length(min = 0, max = 50, message = "长度最多50")
    private String oldName;

    /**
     * 展示名称
     */
    @ApiModelProperty(value = "属性展示名称")
    @Length(min = 0, max = 50, message = "长度最多50")
    private String displayName;

    /**
     * 描述
     */
    @ApiModelProperty(value = "描述")
    @Length(min = 0, max = 200, message = "长度最多50")
    private String desc;


    @ApiModelProperty(value = "列名称")
    private String columnName;

    /**
     * 数据类型
     */
    @ApiModelProperty(value = "数据类型")
    private Integer dataType;

    /**
     * 地图类型：0高德地图，1百度地图
     */
    private Integer mapType;

    /**
     * 数据类型长度
     */
    @ApiModelProperty(value = "数据类型长度")
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
    private Integer displayWidth;

    /**
     * 表单框提示内容
     */
    @ApiModelProperty(value = "表单框提示内容")
    @Length(min = 0, max = 200, message = "长度最多200")
    private String formPrompContent;

    /**
     * 排序序号
     */
    @ApiModelProperty(value = "排序序号")
    private Integer sortWieght;

    /**
     *状态： 0：新增 ，1：修改 ，2:发布 3：删除
     */
    @ApiModelProperty(value = "状态")
    private Integer status;

    /**
     * 发布状态：0：发布失败 1：发布成功
     */
    @ApiModelProperty(value = "发布状态")
    private Integer syncStatus;

    /**
     * 发布失败描述
     */
    @ApiModelProperty(value = "发布失败描述")
    @Length(min = 0, max = 200, message = "长度最多200")
    private String errorMsg;

    /**
     * 是否开启属性日志 0：false 1:true
     */
    @ApiModelProperty(value = "是否开启属性日志")
    private Boolean enableAttributeLog;

    /**
     * 是否只读 0：false 1:true
     */
    @ApiModelProperty(value = "是否只读")
    private Boolean enableReadonly;

    /**
     * 是否必填 0：false 1:true
     */
    @ApiModelProperty(value = "是否必填")
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
    @ApiModelProperty(value = "数据域id（相当于外键）")
    private Integer domainId;

    @ApiModelProperty(value = "属性组id")
    @NotNull()
    private List<Integer> attributeGroupId;


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
