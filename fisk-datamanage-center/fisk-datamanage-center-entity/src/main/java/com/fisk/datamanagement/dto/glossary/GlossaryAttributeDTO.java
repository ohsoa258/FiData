package com.fisk.datamanagement.dto.glossary;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author JianWenYang
 */
@Data
public class GlossaryAttributeDTO extends GlossaryDTO {

    @ApiModelProperty(value = "条款")
    public List<GlossaryTermAttributeDTO> terms;

    @ApiModelProperty(value = "种类")
    public List<GlossaryCategoryAttributeDTO> categories;

}
