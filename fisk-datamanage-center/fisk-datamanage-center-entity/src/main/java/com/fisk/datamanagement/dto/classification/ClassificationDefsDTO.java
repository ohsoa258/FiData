package com.fisk.datamanagement.dto.classification;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author JianWenYang
 */
@Data
public class ClassificationDefsDTO {

    @ApiModelProperty(value = "classificationDefs")
    public List<ClassificationDefContentDTO> classificationDefs;
}
