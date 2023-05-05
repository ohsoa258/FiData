package com.fisk.datamanagement.dto.category;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author JianWenYang
 */
@Data
public class CategoryParentDTO {

    @ApiModelProperty(value = "类guid")
    public String categoryGuid;

    @ApiModelProperty(value = "关联guid")
    public String relationGuid;
}
