package com.fisk.datamanagement.dto.category;

import com.fisk.datamanagement.dto.glossary.GlossaryAnchorDTO;
import com.fisk.datamanagement.dto.glossary.GlossaryDTO;
import com.fisk.datamanagement.dto.term.TermDetailsDTO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author JianWenYang
 */
@Data
public class CategoryDTO extends GlossaryDTO {
    @ApiModelProperty(value = "锚点")
    public GlossaryAnchorDTO anchor;

    @ApiModelProperty(value = "子类")
    public List<ChildrenCategoryDetailsDTO> childrenCategories;

    @ApiModelProperty(value = "父类")
    public CategoryParentDTO parentCategory;

    @ApiModelProperty(value = "条款")
    public List<TermDetailsDTO> terms;
}
