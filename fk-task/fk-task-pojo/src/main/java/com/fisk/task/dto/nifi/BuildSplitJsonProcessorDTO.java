package com.fisk.task.dto.nifi;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class BuildSplitJsonProcessorDTO extends BaseProcessorDTO {
    public String jsonPathExpression;
}
