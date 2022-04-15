package com.fisk.mdm.vo.attribute;

import com.fisk.mdm.enums.DataTypeEnum;
import com.fisk.mdm.enums.MdmTypeEnum;
import com.fisk.mdm.enums.WhetherTypeEnum;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;


/**
 * @author ChenYa
 * @date 2022/4/14 20:35
 */
@Data
public class AttributeVO {
    @ApiModelProperty(value = "主键")
    public int id;

    @ApiModelProperty(value = "实体id")
    private Integer entityId;

    @ApiModelProperty(value = "属性名称")
    private String name;

    @ApiModelProperty(value = "展示名称")
    private String displayName;

    @ApiModelProperty(value = "描述")
    private String desc;

    @ApiModelProperty(value = "数据类型")
    private DataTypeEnum dataType;

    @ApiModelProperty(value = "数据类型长度")
    private int dataTypeLength;

    @ApiModelProperty(value = "数据格式id")
    private int dataTypeFormatId;

    @ApiModelProperty(value = "展示宽度")
    private int displayWidth;

    @ApiModelProperty(value = "表单框提示内容")
    private String formPrompContent;

    @ApiModelProperty(value = "排序序号")
    private int sortWieght;

    /**
     * 是否开启属性日志 0：false 1:true
     */
    @ApiModelProperty(value = "是否开启属性日志")
    private WhetherTypeEnum enableAttributeLog;

    /**
     * 是否只读 0：false 1:true
     */
    @ApiModelProperty(value = "是否只读")
    private WhetherTypeEnum enableReadonly;

    /**
     * 是否必填 0：false 1:true
     */
    @ApiModelProperty(value = "是否必填")
    private WhetherTypeEnum enableRequired;

    /**
     * 类型：
     * 0:code 1:name 2:业务字段
     */
    @ApiModelProperty(value = "类型")
    private MdmTypeEnum mdmType;

    @ApiModelProperty(value = "数据域id（相当于外键）")
    private int domainId;

    @ApiModelProperty(value = "创建人")
    public String createUser;

    @ApiModelProperty(value = "创建时间")
    public LocalDateTime createTime;

    @ApiModelProperty(value = "更新人")
    public String updateUser;

    @ApiModelProperty(value = "更新时间")
    public LocalDateTime updateTime;
}
