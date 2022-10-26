package com.fisk.dataaccess.dto.systemvariables;

import com.fisk.dataaccess.enums.DeltaTimeParameterTypeEnum;
import com.fisk.dataaccess.enums.SystemVariableTypeEnum;
import lombok.Data;

/**
 * @author JianWenYang
 */
@Data
public class SystemVariablesDTO {

    public Integer tableAccessId;

    /**
     * 系统变量类型:START_TIME, STARTTIME, END_TIME, ENDTIME, QUERY_SQL, HISTORICAL_TIME
     */
    public SystemVariableTypeEnum systemVariableType;

    /**
     * 变量类别:CONSTANT, VARIABLE, THE_DEFAULT_EMPTY
     */
    public DeltaTimeParameterTypeEnum deltaTimeParameterType;

    /**
     * 变量值
     */
    public String variableValue;

}
