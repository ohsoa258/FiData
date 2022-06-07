package com.fisk.datamodel.dto.factattribute;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author JianWenYang
 */
@Data
public class FactAttributeDTO {
    @ApiModelProperty(value = "主键id",required = true)
    public long id;

    @ApiModelProperty(value = "事实表id",required = true)
    public int factId;
    /**
     * 事实表中文字段名称
     */
    @ApiModelProperty(value = "事实表中文字段名称")
    public String factFieldCnName;
    /**
     * 事实表字段类型
     */
    @ApiModelProperty(value = "事实表字段类型")
    public String factFieldType;
    /**
     * 事实表字段长度
     */
    @ApiModelProperty(value = "事实表字段长度")
    public int factFieldLength;
    /**
     * 事实表字段描述
     */
    @ApiModelProperty(value = "事实表字段描述")
    public String factFieldDes;

    /**
     * 事实表英文字段名称
     */
    @ApiModelProperty(value = "事实表英文字段名称")
    public String factFieldEnName;
    /**
     * 属性类型：0:退化事实，1:事实建，2:度量字段
     */
    @ApiModelProperty(value = "属性类型：0:退化事实，1:事实建，2:度量字段",required = true)
    public int attributeType;
    /**
     * 关联维度表id
     */
    @ApiModelProperty(value = "关联维度表id",required = true)
    public int associateDimensionId;
    /**
     * 关联维度字段id
     */
    @ApiModelProperty(value = "关联维度字段id",required = true)
    public int associateDimensionFieldId;
    /**
     * 源表名称
     */
    @ApiModelProperty(value = "源表名称")
    public String sourceTableName;
    /**
     * 源字段名称
     */
    @ApiModelProperty(value = "源字段名称")
    public String sourceFieldName;

    @ApiModelProperty(value = "配置详情(事实key的json配置详情)")
    public String configDetails;
}
