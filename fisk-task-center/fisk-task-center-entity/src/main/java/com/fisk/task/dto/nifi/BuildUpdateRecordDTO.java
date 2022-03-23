package com.fisk.task.dto.nifi;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;
/**
 * @author cfk
 */
@Data
public class BuildUpdateRecordDTO extends BaseProcessorDTO{
    public String recordReader;
    public String recordWriter;
    public String replacementValueStrategy;
    public Map<String,String> filedMap;

}
