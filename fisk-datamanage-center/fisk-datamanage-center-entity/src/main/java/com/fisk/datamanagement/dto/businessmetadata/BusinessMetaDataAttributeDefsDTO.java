package com.fisk.datamanagement.dto.businessmetadata;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author JianWenYang
 */
@Data
public class BusinessMetaDataAttributeDefsDTO {


    @ApiModelProperty(value = "名称")
    public String name;

    @ApiModelProperty(value = "类型名称")
    public String typeName;

    @ApiModelProperty(value = "是否可选")
    @JsonProperty(value = "isOptional")
    public boolean isOptional;

    @ApiModelProperty(value = "关联基数")
    public String cardinality;

    @ApiModelProperty(value = "值最小计数")
    public int valuesMinCount;
    @ApiModelProperty(value = "值最大计数")
    public int valuesMaxCount;

    @ApiModelProperty(value = "是否唯一")
    @JsonProperty(value = "isUnique")
    public boolean isUnique;

    @ApiModelProperty(value = "是否可索引")
    @JsonProperty(value = "isIndexable")
    public boolean isIndexable;

    @ApiModelProperty(value = "选项")
    public BusinessMetaDataOptionsDTO options;

    @ApiModelProperty(value = "搜寻重量")
    public String searchWeight;

    @ApiModelProperty(value = "多用价值选择")
    public String multiValueSelect;

}
