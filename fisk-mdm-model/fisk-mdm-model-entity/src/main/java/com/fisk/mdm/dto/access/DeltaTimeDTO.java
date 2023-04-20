package com.fisk.mdm.dto.access;


import com.fisk.mdm.enums.DeltaTimeParameterTypeEnum;
import com.fisk.mdm.enums.SystemVariableTypeEnum;
import lombok.Data;

/**
 * @author jianwenyang
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
