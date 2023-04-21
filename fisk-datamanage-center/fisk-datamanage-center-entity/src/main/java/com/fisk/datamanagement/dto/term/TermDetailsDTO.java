package com.fisk.datamanagement.dto.term;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author JianWenYang
 */
@Data
public class TermDetailsDTO {

    @ApiModelProperty(value = "展示文本")
    public String displayText;
    @ApiModelProperty(value = "relationGuid")
    public String relationGuid;
    @ApiModelProperty(value = "termGuid")
    public String termGuid;

}
