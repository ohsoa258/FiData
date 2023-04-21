package com.fisk.datamanagement.dto.glossary;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author JianWenYang
 */
@Data
public class GlossaryCategoryAttributeDTO extends GlossaryTermCategoryDTO {

    @ApiModelProperty(value = "父类别guid")
    public String parentCategoryGuid;

    @ApiModelProperty(value = "类别guid")
    public String categoryGuid;
}
