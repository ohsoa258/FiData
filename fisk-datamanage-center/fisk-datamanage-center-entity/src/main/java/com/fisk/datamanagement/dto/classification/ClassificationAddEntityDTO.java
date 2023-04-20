package com.fisk.datamanagement.dto.classification;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author JianWenYang
 */
@Data
public class ClassificationAddEntityDTO {

    @ApiModelProperty(value = "分类")
    public ClassificationDTO classification;

    @ApiModelProperty(value = "实体guid")
    public List<String> entityGuids;
}
