package com.fisk.datamodel.dto.table;

import com.fisk.dataservice.enums.DataDoFieldTypeEnum;
import lombok.Data;

/**
 * @author Lock
 */
@Data
public class TableData {

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

}
