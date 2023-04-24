package com.fisk.mdm.dto.attribute;

import com.fisk.mdm.enums.DataRuleEnum;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * @author ChenYa
 */
@Data
public class AttributeDTO {

    /**
     * id
     */
    @ApiModelProperty(value = "id")
    private Integer id;

    @ApiModelProperty(value = "实体id")
    @NotNull()
    private Integer entityId;

    @ApiModelProperty(value = "属性名称")
    @NotNull()
    @Length(max = 50, message = "长度最多50")
    private String name;

    /**
     * 展示名称
     */
    @ApiModelProperty(value = "属性展示名称")
    @NotNull()
    @Length(max = 50, message = "长度最多50")
    private String displayName;

    /**
     * 描述
     */
    @ApiModelProperty(value = "描述")
    @Length(max = 200, message = "长度最多50")
    private String desc;


    @ApiModelProperty(value = "列名")
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
     * 数据规则
    */
    @ApiModelProperty(value = "数据规则")
    @NotNull()
    private Integer dataRule;

    /**
     * 表单框提示内容
     */
    @ApiModelProperty(value = "表单框提示内容")
    @Length(max = 200, message = "长度最多200")
    private String formPrompContent;

    /**
     * 排序序号
     */
    @ApiModelProperty(value = "排序序号")
    @NotNull()
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
    @Length(max = 200, message = "长度最多200")
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
     * 地图类型：0高德地图，1百度地图
     */
    @ApiModelProperty(value = "地图类型：0高德地图，1百度地图")
    private Integer mapType;

    /**
     * 数据域id（相当于外键）
     */
    @ApiModelProperty(value = "数据域id（相当于外键）")
    private Integer domainId;

    @ApiModelProperty(value = "属性组id")
    @NotNull()
    private List<Integer> attributeGroupId;
}
