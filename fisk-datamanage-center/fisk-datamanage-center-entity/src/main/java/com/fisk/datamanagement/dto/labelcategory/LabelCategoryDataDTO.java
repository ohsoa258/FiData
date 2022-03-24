package com.fisk.datamanagement.dto.labelcategory;

import lombok.Data;

import java.util.List;

/**
 * @author JianWenYang
 */
@Data
public class LabelCategoryDataDTO extends LabelCategoryDTO {
    public List<LabelCategoryDataDTO> childrenDto;
}
