package com.fisk.common.service.metadata.dto.metadata;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author JianWenYang
 * @date 2022-07-01 14:36
 */
@Data
public class MetaDataColumnAttributeDTO extends MetaDataBaseAttributeDTO {
    @ApiModelProperty(value = "字段数据类型")
    public String dataType;
}
