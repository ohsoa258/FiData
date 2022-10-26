package com.fisk.dataaccess.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fisk.common.core.baseObject.entity.BasePO;
import com.fisk.dataaccess.enums.DeltaTimeParameterTypeEnum;
import com.fisk.dataaccess.enums.SystemVariableTypeEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author JianWenYang
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("tb_system_variables")
public class SystemVariablesPO extends BasePO {

    public Long tableAccessId;

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
