package com.fisk.task.dto.nifi;

import lombok.Data;

/**
 * @author cfk
 */
@Data
public class BuildAvroReaderServiceDTO extends BaseProcessorDTO{

    public String schemaAccessStrategy;
    public String schemaText;

}
