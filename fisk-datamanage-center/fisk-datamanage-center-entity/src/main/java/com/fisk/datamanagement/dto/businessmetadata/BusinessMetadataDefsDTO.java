package com.fisk.datamanagement.dto.businessmetadata;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author JianWenYang
 */
@Data
public class BusinessMetadataDefsDTO {

    @ApiModelProperty(value = "元数据类别,默认值为BUSINESS_METADATA。不能修改",required = true)
    public String category;

    @ApiModelProperty(value = "guid")
    public String guid;

    @ApiModelProperty(value = "创建者标识")
    public String createdBy;

    @ApiModelProperty(value = "更新者标识")
    public String updatedBy;

    @ApiModelProperty(value = "创建时间")
    public Long createTime;

    @ApiModelProperty(value = "更新时间")
    public Long updateTime;

    @ApiModelProperty(value = "版本")
    public int version;

    @ApiModelProperty(value = "姓名")
    public String name;

    @ApiModelProperty(value = "描述")
    public String description;

    @ApiModelProperty(value = "类型版本")
    public String typeVersion;
    @ApiModelProperty(value = "属性Defs")
    public List<BusinessMetaDataAttributeDefsDTO> attributeDefs;

}
