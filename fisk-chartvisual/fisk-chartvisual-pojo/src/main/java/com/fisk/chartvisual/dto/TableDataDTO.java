package com.fisk.chartvisual.dto;

import com.fisk.chartvisual.enums.DataDoFieldTypeEnum;
import lombok.Data;

/**
 * @author Lock
 */
@Data
public class TableDataDTO {
    /**
     * 字段所属表id
     */
    public Integer id;
    /**
     * 字段类型: 筛选器  列  值
     */
    public DataDoFieldTypeEnum type;
    /**
     * 表所属字段
     */
    public String tableField;
    /**
     * 表名
     */
    public String tableName;
    /**
     * 是否维度 0 否  1 是维度
     */
    public int dimension;
    /**
     * 别名
     */
    public String alias;
    /**
     * 关联哪个维度表id
     */
    public Integer relationId;
    /**
     * 关联维度的表名
     */
    public String dimensionName;
    /**
     * 表名的key
     */
    public String tableNameKey;
}
