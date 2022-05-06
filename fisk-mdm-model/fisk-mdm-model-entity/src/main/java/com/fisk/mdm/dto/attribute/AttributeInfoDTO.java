package com.fisk.mdm.dto.attribute;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fisk.common.core.baseObject.vo.BaseUserInfoVO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * @Author WangYan
 * @Date 2022/4/21 15:09
 * @Version 1.0
 */
@Data
public class AttributeInfoDTO extends BaseUserInfoVO {

    /**
     * id
     */
    private Integer id;

    /**
     * 模型id
     */
    private Integer modelId;

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
    private String desc;

    /**
     * 底层表名
     */
    private String columnName;

    /**
     * 数据类型
     */
    private String dataType;

    /**
     * 数据类型长度
     */
    private Integer dataTypeLength;

    /**
     * 数据类型小数点长度
     */
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
     * 状态： 0：新增 ，1：修改 ，2:发布 3：删除
     */
    private String status;

    /**
     * 提交状态：0：提交失败 1：提交成功
     */
    private String syncStatus;

    /**
     * 提交失败描述
     */
    private String errorMsg;

    /**
     * 是否开启属性日志 0：false 1:true
     */
    private Boolean enableAttributeLog;

    /**
     * 是否只读 0：false 1:true
     */
    private Boolean enableReadonly;

    /**
     * 是否必填 0：false 1:true
     */
    private Boolean enableRequired;

    /**
     * 类型：
     * 0:code 1:name 2:业务字段
     */
    private String mdmType;

    /**
     * 数据域id（相当于外键）
     */
    private Integer domainId;

    /**
     * 关联实体名称
     */
    private String domainName;

    /**
     * 关联实体id
     */
    private Integer domainEntityId;

    @JsonFormat(shape = JsonFormat.Shape.STRING,pattern="yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @ApiModelProperty(value = "创建时间",required = true)
    public LocalDateTime createTime;

    @JsonFormat(shape = JsonFormat.Shape.STRING,pattern="yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @ApiModelProperty(value = "更新时间",required = true)
    public LocalDateTime updateTime;
}
