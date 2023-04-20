package com.fisk.datamanagement.dto.glossary;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author JianWenYang
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class GlossaryTermCategoryDTO extends GlossaryDTO {

    @ApiModelProperty(value = "关联guid")
    public String relationGuid;

    @ApiModelProperty(value = "展示文本")
    public String displayText;

}
