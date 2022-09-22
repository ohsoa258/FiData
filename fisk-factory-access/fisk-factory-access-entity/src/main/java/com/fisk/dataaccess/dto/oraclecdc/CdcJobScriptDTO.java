package com.fisk.dataaccess.dto.oraclecdc;

import lombok.Data;

/**
 * @author JianWenYang
 */
@Data
public class CdcJobScriptDTO {
    /**
     * flink创建来源表脚本
     */
    public String sourceTableScript;
    /**
     * flink创建目标表脚本
     */
    public String targetTableScript;
    /**
     * 执行sql
     */
    public String sqlScript;

}
