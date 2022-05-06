package com.fisk.mdm.vo.attribute;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fisk.common.core.baseObject.vo.BaseUserInfoVO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;


/**
 * @author ChenYa
 * @date 2022/4/14 20:35
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class AttributeVO extends BaseUserInfoVO {
    @ApiModelProperty(value = "主键")
    public int id;

    @ApiModelProperty(value = "实体id")
    private Integer entityId;

    @ApiModelProperty(value = "模型id")
    private Integer modelId;

    @ApiModelProperty(value = "属性名称")
    private String name;

    @ApiModelProperty(value = "展示名称")
    private String displayName;

    @ApiModelProperty(value = "描述")
    private String desc;

    @ApiModelProperty(value = "底层表名")
    private String columnName;

    @ApiModelProperty(value = "数据类型")
    private String dataType;

    @ApiModelProperty(value = "数据类型长度")
    private Integer dataTypeLength;

    /**
     * 数据类型小数点长度
     */
    @ApiModelProperty(value = "数据类型小数点长度")
    private Integer dataTypeDecimalLength;

    @ApiModelProperty(value = "数据格式id")
    private Integer dataTypeFormatId;

    @ApiModelProperty(value = "展示宽度")
    private Integer displayWidth;

    @ApiModelProperty(value = "表单框提示内容")
    private String formPrompContent;

    @ApiModelProperty(value = "排序序号")
    private Integer sortWieght;

    /**
     *状态： 0：新增 ，1：修改 ，2:发布 3：删除
     */
    @ApiModelProperty(value = "状态")
    private String status;

    /**
     * 提交状态：0：提交失败 1：提交成功
     */
    @ApiModelProperty(value = "提交状态")
    private String syncStatus;

    /**
     * 提交失败描述
     */
    @ApiModelProperty(value = "提交失败描述")
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
    private String mdmType;

    @ApiModelProperty(value = "数据域id（相当于外键）")
    private Integer domainId;

    @ApiModelProperty(value = "创建时间")
    @JsonFormat(shape = JsonFormat.Shape.STRING,pattern="yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    public LocalDateTime createTime;

    @ApiModelProperty(value = "更新时间")
    @JsonFormat(shape = JsonFormat.Shape.STRING,pattern="yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    public LocalDateTime updateTime;
}
