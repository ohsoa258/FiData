package com.fisk.common.service.metadata.dto.metadata;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author JianWenYang
 * @date 2022-07-01 14:33
 */
@Data
public class MetaDataTableAttributeDTO extends MetaDataBaseAttributeDTO {
    @ApiModelProperty(value = "字段集合")
    public List<MetaDataColumnAttributeDTO> columnList;
}
