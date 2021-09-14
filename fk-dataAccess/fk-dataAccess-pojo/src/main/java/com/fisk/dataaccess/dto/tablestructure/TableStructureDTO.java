package com.fisk.dataaccess.dto.tablestructure;

import lombok.Data;

/**
 * <p>
 *     表结构对象
 * </p>
 * @author Lock
 */
@Data
public class TableStructureDTO {
    /**
     * 字段名
     */
    public String fieldName;
    /**
     * 字段类型
     */
    public String fieldType;
    /**
     * 字段长度
     */
    public int fieldLength;

    /**
     * 字段描述
     */
    public String fieldDes;
}
