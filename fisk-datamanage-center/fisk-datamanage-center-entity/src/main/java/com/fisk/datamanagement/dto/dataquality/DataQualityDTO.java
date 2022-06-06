package com.fisk.datamanagement.dto.dataquality;

import lombok.Data;

/**
 * @author JianWenYang
 */
@Data
public class DataQualityDTO {

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
    public String port;
    /**
     * 数据库类型
     */
    public String rdbmsType;

}
