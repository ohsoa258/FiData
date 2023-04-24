package com.fisk.task.dto.nifi;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author cfk
 */
@Data
public class BuildAvroReaderServiceDTO extends BaseProcessorDTO{

    @ApiModelProperty(value = "模式访问策略")
    public String schemaAccessStrategy;
    @ApiModelProperty(value = "文本模式")
    public String schemaText;

}
