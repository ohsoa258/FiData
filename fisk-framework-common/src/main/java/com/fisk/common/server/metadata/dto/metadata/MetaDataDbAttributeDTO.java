package com.fisk.common.server.metadata.dto.metadata;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author JianWenYang
 * @date 2022-07-01 14:30
 */
@Data
public class MetaDataDbAttributeDTO extends MetaDataBaseAttributeDTO {
    @ApiModelProperty(value = "表集合")
    public List<MetaDataTableAttributeDTO> tableList;
}
