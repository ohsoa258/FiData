package com.fisk.task.dto.nifi;

import lombok.Data;

/**
 * @author cfk
 */
@Data
public class BuildConvertRecordProcessorDTO extends BaseProcessorDTO {
    public String recordReader;
    public String recordWriter;
}
