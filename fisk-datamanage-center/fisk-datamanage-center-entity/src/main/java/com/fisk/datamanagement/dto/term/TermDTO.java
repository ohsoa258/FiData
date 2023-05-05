package com.fisk.datamanagement.dto.term;

import com.fisk.datamanagement.dto.category.CategoryDetailsDTO;
import com.fisk.datamanagement.dto.glossary.GlossaryAnchorDTO;
import com.fisk.datamanagement.dto.glossary.GlossaryDTO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author JianWenYang
 */
@Data
public class TermDTO extends GlossaryDTO {

    @ApiModelProperty(value = "锚点")
    public GlossaryAnchorDTO anchor;

    /**
     * 术语关联类别
     */
    @ApiModelProperty(value = "范畴")
    public List<CategoryDetailsDTO> categories;

}
