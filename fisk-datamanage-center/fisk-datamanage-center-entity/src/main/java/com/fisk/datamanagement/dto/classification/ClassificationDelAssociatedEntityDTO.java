package com.fisk.datamanagement.dto.classification;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author JianWenYang
 */
@Data
public class ClassificationDelAssociatedEntityDTO {
    @ApiModelProperty(value = "实体guid",required = true)
    public String entityGuid;
    @ApiModelProperty(value = "分类名称",required = true)
    public String classificationName;

}
