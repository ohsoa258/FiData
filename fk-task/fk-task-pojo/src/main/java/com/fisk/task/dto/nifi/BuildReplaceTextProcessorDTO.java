package com.fisk.task.dto.nifi;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author gy
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class BuildReplaceTextProcessorDTO extends BaseProcessorDTO{
    public String replacementValue;
}
