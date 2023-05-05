package com.fisk.datamanagement.dto.process;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author JianWenYang
 */
@Data
public class ProcessRelationshipAttributesDTO {

    @ApiModelProperty(value = "输出")
    public List<ProcessRelationshipAttributesPutDTO> outputs;

    @ApiModelProperty(value = "输入")
    public List<ProcessRelationshipAttributesPutDTO> inputs;

}
