package com.fisk.dataaccess.dto.access;


import com.fisk.dataaccess.enums.DeltaTimeParameterTypeEnum;
import com.fisk.dataaccess.enums.SystemVariableTypeEnum;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author cfk
 */
@Data
public class DeltaTimeDTO {
    /*
     *变量名称
     */
    @ApiModelProperty(value = "变量名称")
    public SystemVariableTypeEnum systemVariableTypeEnum;
    /*
     *变量类别
     */
    @ApiModelProperty(value = "变量类别")
    public DeltaTimeParameterTypeEnum deltaTimeParameterTypeEnum;
    /*
     *变量值
     */
    @ApiModelProperty(value = "变量值")
    public String variableValue;

}
