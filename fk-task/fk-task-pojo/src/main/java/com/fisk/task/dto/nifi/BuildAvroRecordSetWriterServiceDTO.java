package com.fisk.task.dto.nifi;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author cfk
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class BuildAvroRecordSetWriterServiceDTO extends BaseProcessorDTO {

    public String schemaArchitecture;
}
