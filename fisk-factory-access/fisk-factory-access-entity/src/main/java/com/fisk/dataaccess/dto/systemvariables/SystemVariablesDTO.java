package com.fisk.dataaccess.dto.systemvariables;

import com.fisk.dataaccess.enums.DeltaTimeParameterTypeEnum;
import com.fisk.dataaccess.enums.SystemVariableTypeEnum;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author JianWenYang
 */
@Data
public class SystemVariablesDTO {

    @ApiModelProperty(value = "目标表ID")
    public Integer tableAccessId;

    /**
     * 系统变量类型:START_TIME, STARTTIME, END_TIME, ENDTIME, QUERY_SQL, HISTORICAL_TIME
     */
    @ApiModelProperty(value = "系统变量类型")
    public SystemVariableTypeEnum systemVariableType;

    /**
     * 变量类别:CONSTANT, VARIABLE, THE_DEFAULT_EMPTY
     */
    @ApiModelProperty(value = "delta时间参数类型")
    public DeltaTimeParameterTypeEnum deltaTimeParameterType;

    /**
     * 变量值
     */
    @ApiModelProperty(value = "变量值")
    public String variableValue;

}
