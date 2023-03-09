package com.fisk.datamodel.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fisk.common.core.baseObject.entity.BasePO;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author JianWenYang
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("tb_system_variables")
public class SystemVariablesPO extends BasePO {

    public Integer tableAccessId;

    /**
     * 系统变量类型:START_TIME, STARTTIME, END_TIME, ENDTIME, QUERY_SQL, HISTORICAL_TIME
     */
    public String systemVariableType;

    /**
     * 变量类别:CONSTANT, VARIABLE, THE_DEFAULT_EMPTY
     */
    public String deltaTimeParameterType;

    /**
     * 变量值
     */
    public String variableValue;

    /**
     * 表類型类型
     */
    public Integer type;

}
