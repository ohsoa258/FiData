package com.fisk.task.dto.nifi;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;
/**
 * @author cfk
 */
@Data
public class BuildUpdateRecordDTO extends BaseProcessorDTO{
    @ApiModelProperty(value = "记录读取器")
    public String recordReader;
    @ApiModelProperty(value = "记录写入器")
    public String recordWriter;
    @ApiModelProperty(value = "重置值策略")
    public String replacementValueStrategy;
    @ApiModelProperty(value = "文件映像")
    public Map<String,String> filedMap;

}
