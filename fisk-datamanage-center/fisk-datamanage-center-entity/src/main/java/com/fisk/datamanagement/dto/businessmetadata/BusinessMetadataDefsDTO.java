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

    public String guid;

    public String createdBy;

    public String updatedBy;

    public Long createTime;

    public Long updateTime;

    public int version;

    public String name;

    public String description;

    public String typeVersion;

    public List<BusinessMetaDataAttributeDefsDTO> attributeDefs;


}
