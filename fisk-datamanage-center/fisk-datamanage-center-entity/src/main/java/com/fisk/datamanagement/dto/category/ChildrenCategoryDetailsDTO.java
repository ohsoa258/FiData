package com.fisk.datamanagement.dto.category;

import lombok.Data;

/**
 * @author JianWenYang
 */
@Data
public class ChildrenCategoryDetailsDTO extends CategoryDetailsDTO {

    public String parentCategoryGuid;

}
