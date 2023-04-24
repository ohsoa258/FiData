package com.fisk.datamanagement.dto.process;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author JianWenYang
 */
@Data
    public class ProcessEntityDTO {

    @ApiModelProperty(value = "类型名称")
    public String typeName;

    @ApiModelProperty(value = "guid")
    public String guid;

    @ApiModelProperty(value = "状态")
    public String status;

    @ApiModelProperty(value = "属性")
    public ProcessAttributesDTO attributes;

    @ApiModelProperty(value = "关系属性")
    public ProcessRelationshipAttributesDTO relationshipAttributes;

}
