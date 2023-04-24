package com.fisk.datamanagement.dto.category;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author JianWenYang
 */
@Data
public class CategoryDetailsDTO {

    @ApiModelProperty(value = "展示文本")
    public String displayText;

    @ApiModelProperty(value = "关联guid")
    public String relationGuid;

    @ApiModelProperty(value = "范畴guid")
    public String categoryGuid;

}
