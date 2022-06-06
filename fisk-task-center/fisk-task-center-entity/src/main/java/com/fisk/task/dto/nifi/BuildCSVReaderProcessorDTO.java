package com.fisk.task.dto.nifi;

import lombok.Data;

/**
 * @author cfk
 */
@Data
public class BuildCSVReaderProcessorDTO extends BaseProcessorDTO {
    public String schemaAccessStrategy;
    public String schemaText;
}
