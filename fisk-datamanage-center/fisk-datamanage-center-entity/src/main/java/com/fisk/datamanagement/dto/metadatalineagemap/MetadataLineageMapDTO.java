package com.fisk.datamanagement.dto.metadatalineagemap;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author JianWenYang
 */
@Data
public class MetadataLineageMapDTO {

    @ApiModelProperty(value = "展示文件")
    public String displayText;

    @ApiModelProperty(value = "描述")
    public String description;

    @ApiModelProperty(value = "类型名称")
    public String typeName;

}
