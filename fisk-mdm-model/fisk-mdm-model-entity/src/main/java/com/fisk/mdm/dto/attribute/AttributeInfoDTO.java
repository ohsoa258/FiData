package com.fisk.mdm.dto.attribute;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fisk.common.core.baseObject.vo.BaseUserInfoVO;
import com.fisk.mdm.dto.attributeGroup.AttributeGroupDTO;
import com.fisk.mdm.enums.DataRuleEnum;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

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
    @ApiModelProperty(value = "id")
    private Integer id;

    /**
     * 模型id
     */
    @ApiModelProperty(value = "模型id")
    private Integer modelId;

    /**
     * 实体id
     */
    @ApiModelProperty(value = "实体id")
    private Integer entityId;

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
    private String desc;

    /**
     * 底层表名
     */
    @ApiModelProperty(value = "底层表名")
    private String columnName;

    /**
     * 数据类型
     */
    @ApiModelProperty(value = "数据类型")
    private String dataType;

    @ApiModelProperty(value = "数据类型英文展示")
    public String dataTypeEnDisplay;

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
     * 数据规则 0 四舍五入 1 截取
     */
    @ApiModelProperty(value = "数据规则 0 四舍五入 1 截取")
    private Integer dataRule;
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
     * 状态： 0：新增 ，1：修改 ，2:已发布 3：删除
     */
    @ApiModelProperty(value = "状态： 0：新增 ，1：修改 ，2:已发布 3：删除")
    private String status;

    /**
     * 发布状态：0：发布失败 1：发布成功
     */
    @ApiModelProperty(value = "发布状态：0：发布失败 1：发布成功")
    private String syncStatus;

    /**
     * 发布失败描述
     */
    @ApiModelProperty(value = "发布失败描述")
    private String errorMsg;

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
    @ApiModelProperty(value = "0:code 1:name 2:业务字段")
    private String mdmType;

    /**
     * 地图类型：0高德地图，1百度地图
     */
    @ApiModelProperty(value = "地图类型：0高德地图，1百度地图")
    private String mapType;

    /**
     * 数据域id（相当于外键）
     */
    @ApiModelProperty(value = "数据域id（相当于外键）")
    private Integer domainId;

    /**
     * 关联实体名称
     */
    @ApiModelProperty(value = "关联实体名称")
    private String domainName;

    /**
     * 关联实体id
     */
    @ApiModelProperty(value = "关联实体id")
    private Integer domainEntityId;

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
