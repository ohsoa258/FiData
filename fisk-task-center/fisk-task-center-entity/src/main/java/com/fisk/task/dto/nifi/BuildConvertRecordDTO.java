package com.fisk.task.dto.nifi;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Map;

@Data
public class BuildConvertRecordDTO extends BaseProcessorDTO {

    @ApiModelProperty(value = "记录读取器")
    public String recordReader;
    @ApiModelProperty(value = "记录写入器")
    public String recordWriter;

}
