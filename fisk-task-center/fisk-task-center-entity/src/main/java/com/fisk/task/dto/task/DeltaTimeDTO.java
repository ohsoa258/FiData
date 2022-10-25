package com.fisk.task.dto.task;

import com.fisk.dataaccess.enums.DeltaTimeParameterTypeEnum;
import com.fisk.dataaccess.enums.SystemVariableTypeEnum;
import lombok.Data;

/**
 * @author cfk
 */
@Data
public class DeltaTimeDTO {
    /*
     *变量名称
     */
    public SystemVariableTypeEnum systemVariableTypeEnum;
    /*
     *变量类别
     */
    public DeltaTimeParameterTypeEnum deltaTimeParameterTypeEnum;
    /*
     *变量值
     */
    public String variableValue;

}
