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
    /**
     * Schema Write Strategy  一般值为avro-embedded
     */
    public String schemaWriteStrategy;
    /**
     * schema-access-strategy   schema-text-property
     */
    public String schemaAccessStrategy;


}
