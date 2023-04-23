package com.fisk.task.dto.nifi;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author cfk
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class BuildAvroRecordSetWriterServiceDTO extends BaseProcessorDTO {

    @ApiModelProperty(value = "模式架构")
    public String schemaArchitecture;
    /**
     * Schema Write Strategy  一般值为avro-embedded
     */
    @ApiModelProperty(value = "模式写策略")
    public String schemaWriteStrategy;
    /**
     * schema-access-strategy   schema-text-property
     */
    @ApiModelProperty(value = "模式访问策略")
    public String schemaAccessStrategy;


}
