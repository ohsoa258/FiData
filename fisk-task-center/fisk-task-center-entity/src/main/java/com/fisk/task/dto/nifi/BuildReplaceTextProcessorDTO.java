package com.fisk.task.dto.nifi;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author gy
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class BuildReplaceTextProcessorDTO extends BaseProcessorDTO {
    /*
     * Replacement Value
     * */
    @ApiModelProperty(value = "重置值")
    public String replacementValue;
    /*
     * Evaluation Mode
     * */
    @ApiModelProperty(value = "评估模式")
    public String evaluationMode;

    /*
    * Maximum Buffer Size
    * */
    @ApiModelProperty(value = "最大缓冲区大小")
    public String maximumBufferSize;
}
