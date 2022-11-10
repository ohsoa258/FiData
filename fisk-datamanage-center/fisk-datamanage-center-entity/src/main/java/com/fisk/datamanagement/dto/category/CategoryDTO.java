package com.fisk.datamanagement.dto.category;

import com.fisk.datamanagement.dto.glossary.GlossaryAnchorDTO;
import com.fisk.datamanagement.dto.glossary.GlossaryDTO;
import com.fisk.datamanagement.dto.term.TermDetailsDTO;
import lombok.Data;

import java.util.List;

/**
 * @author JianWenYang
 */
@Data
public class CategoryDTO extends GlossaryDTO {
    public GlossaryAnchorDTO anchor;
    public List<ChildrenCategoryDetailsDTO> childrenCategories;
    public CategoryParentDTO parentCategory;
    public List<TermDetailsDTO> terms;
}
