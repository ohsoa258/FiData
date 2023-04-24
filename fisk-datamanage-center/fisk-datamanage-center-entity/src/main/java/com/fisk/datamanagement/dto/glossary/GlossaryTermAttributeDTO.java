package com.fisk.datamanagement.dto.glossary;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author JianWenYang
 */
@Data
public class GlossaryTermAttributeDTO extends GlossaryTermCategoryDTO {

    @ApiModelProperty(value = "术语guid")
    public String termGuid;

}
