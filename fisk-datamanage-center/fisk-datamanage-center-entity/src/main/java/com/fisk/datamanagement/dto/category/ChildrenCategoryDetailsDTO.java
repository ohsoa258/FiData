package com.fisk.datamanagement.dto.category;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author JianWenYang
 */
@Data
public class ChildrenCategoryDetailsDTO extends CategoryDetailsDTO {

    @ApiModelProperty(value = "父类别指南")
    public String parentCategoryGuid;

}
