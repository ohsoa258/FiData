package com.fisk.datamanagement.dto.dataquality;

import lombok.Data;

/**
 * @author JianWenYang
 */
@Data
public class UpperLowerBloodParameterDTO {

    /**
     * 1:上游血缘、2：下游血缘、3：上下级血缘
     */
    public int type;
    /**
     * 表名
     */
    public String tableName;
    /**
     * 配置文件索引
     */
    public int index;

}
