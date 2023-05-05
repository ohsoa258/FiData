package com.fisk.mdm.dto.attributelog;

import com.baomidou.mybatisplus.annotation.TableField;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fisk.common.core.baseObject.vo.BaseUserInfoVO;
import com.fisk.mdm.dto.attributeGroup.AttributeGroupDTO;
import com.fisk.mdm.enums.MdmTypeEnum;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @Author WangYan
 * @Date 2022/6/14 15:42
 * @Version 1.0
 */
@Data
public class AttributeLogDTO extends BaseUserInfoVO {

    /**
     * 主键id
     */
    @ApiModelProperty(value = "主键id")
    private Integer id;

    /**
     * 实体id
     */
    @ApiModelProperty(value = "实体id")
    private Integer entityId;

    /**
     * 属性id
     */
    @ApiModelProperty(value = "属性id")
    private Integer attributeId;

    /**
     * 标记
     */
    @ApiModelProperty(value = "标记")
    private String mark;

    /**
     * 属性名称
     */
    @ApiModelProperty(value = "属性名称")
    private String name;

    /**
     * 展示名称
     */
    @ApiModelProperty(value = "展示名称")
    private String displayName;

    /**
     * 描述
     */
    @ApiModelProperty(value = "描述")
    @TableField(value = "`desc`")
    private String desc;

    /**
     * 列名（底层表中通过编码规则创建出来的列名）
     */
    @ApiModelProperty(value = "列名（底层表中通过编码规则创建出来的列名）")
    private String columnName;

    /**
     * 数据类型
     */
    @ApiModelProperty(value = "数据类型")
    private String dataType;

    /**
     * 数据类型长度
     */
    @ApiModelProperty(value = "数据类型长度")
    private Integer dataTypeLength;

    /**
     * 数据类型枚举名称
     */
    @ApiModelProperty(value = "数据类型枚举名称")
    private String dataTypeEnDisplay;

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
    private String formPrompContent;

    /**
     * 排序序号
     */
    @ApiModelProperty(value = "排序序号")
    private Integer sortWieght;

    /**
     * 是否开启属性日志 0：false 1:true
     */
    @ApiModelProperty(value = "是否开启属性日志 0：false 1:true")
    private Boolean enableAttributeLog;

    /**
     * 是否只读 0：false 1:true
     */
    @ApiModelProperty(value = "是否只读 0：false 1:true")
    private Boolean enableReadonly;

    /**
     * 是否必填 0：false 1:true
     */
    @ApiModelProperty(value = "是否必填 0：false 1:true")
    private Boolean enableRequired;

    /**
     * 类型：
     * 0:code 1:name 2:业务字段
     */
    @ApiModelProperty(value = "类型：0:code 1:name 2:业务字段")
    private MdmTypeEnum mdmType;

    /**
     * 数据域id（相当于外键）
     */
    @ApiModelProperty(value = "数据域id（相当于外键）")
    private Integer domainId;

    /**
     * 属性组信息
     */
    @ApiModelProperty(value = "属性组信息")
    private List<AttributeGroupDTO> attributeGroupList;

    @JsonFormat(shape = JsonFormat.Shape.STRING,pattern="yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @ApiModelProperty(value = "创建时间",required = true)
    public LocalDateTime createTime;

    @JsonFormat(shape = JsonFormat.Shape.STRING,pattern="yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @ApiModelProperty(value = "更新时间",required = true)
    public LocalDateTime updateTime;
}
