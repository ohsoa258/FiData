package com.fisk.datamanagement.dto.glossary;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author JianWenYang
 */
@Data
public class GlossaryDTO {
    public String displayText;
    public String termGuid;
    public String guid;
    public String pid;
    public String qualifiedName;
    @ApiModelProperty(value = "名称",required=true)
    public String name;
    @ApiModelProperty(value = "简短的描述")
    public String shortDescription;
    @ApiModelProperty(value = "详细的描述")
    public String longDescription;
}
