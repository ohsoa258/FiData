package com.fisk.datamanagement.dto.glossary;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author JianWenYang
 */
@Data
public class GlossaryDTO {
    @ApiModelProperty(value = "展示文本")
    public String displayText;

    @ApiModelProperty(value = "术语guid")
    public String termGuid;

    @ApiModelProperty(value = "词汇库ID")
    public Integer glossaryLibraryId;

    @ApiModelProperty(value = "guid")
    public String guid;

    @ApiModelProperty(value = "pid")
    public String pid;

    @ApiModelProperty(value = "限定名")
    public String qualifiedName;

    @ApiModelProperty(value = "名称",required=true)
    public String name;
    @ApiModelProperty(value = "简短的描述")
    public String shortDescription;
    @ApiModelProperty(value = "详细的描述")
    public String longDescription;
}
