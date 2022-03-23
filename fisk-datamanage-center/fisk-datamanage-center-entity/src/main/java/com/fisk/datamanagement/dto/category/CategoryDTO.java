package com.fisk.datamanagement.dto.category;

import com.fisk.datamanagement.dto.glossary.GlossaryAnchorDTO;
import com.fisk.datamanagement.dto.glossary.GlossaryDTO;
import lombok.Data;

/**
 * @author JianWenYang
 */
@Data
public class CategoryDTO extends GlossaryDTO {
    public GlossaryAnchorDTO anchor;
    public CategoryParentDTO parentCategory;
}
