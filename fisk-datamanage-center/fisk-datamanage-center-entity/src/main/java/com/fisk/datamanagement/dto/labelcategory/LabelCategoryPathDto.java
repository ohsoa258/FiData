package com.fisk.datamanagement.dto.labelcategory;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class LabelCategoryPathDto extends LabelCategoryDTO  {
    @ApiModelProperty(value = "路径")
    public String path;
}
