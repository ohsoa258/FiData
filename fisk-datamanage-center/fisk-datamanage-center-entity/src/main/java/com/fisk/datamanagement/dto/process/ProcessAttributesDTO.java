package com.fisk.datamanagement.dto.process;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author JianWenYang
 */
@Data
public class ProcessAttributesDTO {

    @ApiModelProperty(value = "限定名")
    public String qualifiedName;

    @ApiModelProperty(value = "名称")
    public String name;

    @ApiModelProperty(value = "描述")
    public String description;

    @ApiModelProperty(value = "输出")
    public List<ProcessAttributesPutDTO> outputs;

    @ApiModelProperty(value = "输入")
    public List<ProcessAttributesPutDTO> inputs;

}
