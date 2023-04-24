package com.fisk.datamanagement.dto.metadatabusinessmetadatamap;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author JianWenYang
 */
@Data
public class MetadataBusinessInfoDTO {

    @ApiModelProperty(value = "属性名")
    public String attributeName;

    @ApiModelProperty(value = "属性中文名")
    public String attributeCnName;

    @ApiModelProperty(value = "业务元数据中文名")
    public String businessMetadataCnName;

    @ApiModelProperty(value = "业务元数据名称")
    public String businessMetadataName;

    @ApiModelProperty(value = "值")
    public String value;

}
