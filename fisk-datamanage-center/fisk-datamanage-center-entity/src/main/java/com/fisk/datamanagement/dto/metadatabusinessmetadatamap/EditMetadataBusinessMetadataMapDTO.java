package com.fisk.datamanagement.dto.metadatabusinessmetadatamap;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author JianWenYang
 */
@Data
public class EditMetadataBusinessMetadataMapDTO {

    @ApiModelProperty(value = "数据源实体id")
    public Integer metadataEntityId;

    @ApiModelProperty(value = "列表")
    public List<MetadataBusinessMetadataMapDTO> list;

}
