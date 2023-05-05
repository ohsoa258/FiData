package com.fisk.datamanagement.dto.relationship;

import com.fisk.datamanagement.dto.process.ProcessAttributesPutDTO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author JianWenYang
 */
@Data
public class RelationshipDTO {
    @ApiModelProperty(value = "guid")
    public String guid;
    @ApiModelProperty(value = "类型名称")
    public String typeName;
    @ApiModelProperty(value = "end1")
    public ProcessAttributesPutDTO end1;
    @ApiModelProperty(value = "end2")
    public ProcessAttributesPutDTO end2;
}
