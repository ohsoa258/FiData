package com.fisk.datamanagement.dto.labelcategory;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author JianWenYang
 */
@Data
public class LabelCategoryDTO {

    @ApiModelProperty(value = "id")
    public int id;
    @ApiModelProperty(value = "类别码")
    public String categoryCode;
    @ApiModelProperty(value = "类别父代码")
    public String categoryParentCode;
    @ApiModelProperty(value = "类别中文名")
    public String categoryCnName;
    @ApiModelProperty(value = "类别英文名")
    public String categoryEnName;
}
