package com.fisk.task.dto.nifi;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author cfk
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class BuildGenerateFlowFileProcessorDTO extends BaseProcessorDTO {
    /*
     *generate-ff-custom-text
     *
     */
    @ApiModelProperty(value = "生成自定义文本")
    public String generateCustomText;


}
