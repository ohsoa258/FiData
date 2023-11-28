package com.fisk.datamanagement.dto.classification;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author xgf
 * @date 2023年11月20日 16:08
 */
@Data
public class BusinessCategoryDTO {

    @ApiModelProperty(value = "classificationDefs")
    public List<BusinessCategoryDefsDTO> classificationDefs;
}
