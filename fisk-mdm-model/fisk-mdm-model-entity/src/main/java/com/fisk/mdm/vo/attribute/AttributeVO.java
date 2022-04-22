package com.fisk.mdm.vo.attribute;

import com.alibaba.fastjson.annotation.JSONField;
import com.fisk.common.core.baseObject.vo.BaseUserInfoVO;
import com.fisk.mdm.enums.AttributeStatusEnum;
import com.fisk.mdm.enums.AttributeSyncStatusEnum;
import com.fisk.mdm.enums.DataTypeEnum;
import com.fisk.mdm.enums.MdmTypeEnum;
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
    private int dataTypeLength;

    /**
     * 数据类型小数点长度
     */
    @ApiModelProperty(value = "数据类型小数点长度")
    private int dataTypeDecimalLength;

    @ApiModelProperty(value = "数据格式id")
    private int dataTypeFormatId;

    @ApiModelProperty(value = "展示宽度")
    private int displayWidth;

    @ApiModelProperty(value = "表单框提示内容")
    private String formPrompContent;

    @ApiModelProperty(value = "排序序号")
    private int sortWieght;

    /**
     *状态： 0：待新增 ，1：待修改 ， 2：已提交
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
    private int domainId;

    @ApiModelProperty(value = "创建时间")
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    public LocalDateTime createTime;

    @ApiModelProperty(value = "更新时间")
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    public LocalDateTime updateTime;
}
