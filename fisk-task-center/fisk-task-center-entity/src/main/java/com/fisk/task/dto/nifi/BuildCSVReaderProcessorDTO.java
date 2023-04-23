package com.fisk.task.dto.nifi;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author cfk
 */
@Data
public class BuildCSVReaderProcessorDTO extends BaseProcessorDTO {

    @ApiModelProperty(value = "模式访问策略")
    public String schemaAccessStrategy;
    @ApiModelProperty(value = "文本模式")
    public String schemaText;

    /**
     * CSV Format
     */
    @ApiModelProperty(value = " CSV格式 /数据存储")
    public String csvFormat;
    /**
     * Skip Header Line
     */
    @ApiModelProperty(value = "跳过标题行")
    public String skipHeaderLine;
}
