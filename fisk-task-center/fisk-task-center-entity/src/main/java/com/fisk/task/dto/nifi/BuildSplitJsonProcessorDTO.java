package com.fisk.task.dto.nifi;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class BuildSplitJsonProcessorDTO extends BaseProcessorDTO {
    @ApiModelProperty(value = "json路径表达式")
    public String jsonPathExpression;
}
