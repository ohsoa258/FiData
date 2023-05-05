package com.fisk.datamanagement.dto.labelcategory;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author JianWenYang
 */
@Data
public class LabelCategoryDataDTO extends LabelCategoryDTO {

    @ApiModelProperty(value = "子类dto")
    public List<LabelCategoryDataDTO> childrenDto;
}
