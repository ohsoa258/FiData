package com.fisk.datamodel.dto.widetablerelationconfig;

import lombok.Data;

/**
 * @author JianWenYang
 */
@Data
public class WideTableRelationConfigDTO {

    public Integer id;
    /**
     * 源表名
     */
    public String sourceTable;
    /**
     * 源字段名称
     */
    public String sourceColumn;
    /**
     * 连接类型: left join, join, right join...
     */
    public String joinType;
    /**
     * 目标表名
     */
    public String targetTable;
    /**
     * 目标字段
     */
    public String targetColumn;
}
