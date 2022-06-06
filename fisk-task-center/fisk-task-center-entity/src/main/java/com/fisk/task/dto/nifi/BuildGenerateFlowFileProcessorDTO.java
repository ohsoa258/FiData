package com.fisk.task.dto.nifi;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author cfk
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class BuildGenerateFlowFileProcessorDTO extends BaseProcessorDTO {
    /*
     *generate-ff-custom-text
     *
     */
    public String generateCustomText;


}
