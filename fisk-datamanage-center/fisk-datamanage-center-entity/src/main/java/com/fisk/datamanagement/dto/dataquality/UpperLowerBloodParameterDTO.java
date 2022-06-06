package com.fisk.datamanagement.dto.dataquality;

import lombok.Data;

/**
 * @author JianWenYang
 */
@Data
public class UpperLowerBloodParameterDTO {

    /**
     * 1:上游、2：下游、3：上下血缘
     */
    public int checkConsanguinity;
    /**
     * 实例名
     */
    public String instanceName;
    /**
     * 库名
     */
    public String dbName;
    /**
     * 表名
     */
    public String tableName;

}
