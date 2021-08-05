package com.fisk.dataaccess.dto;

import lombok.Data;

import java.util.List;

/**
 * @author Lock
 */
@Data
public class TableNameDTO {

    /**
     * 表名
     */
    public String tableName;

    /**
     * 表id
     */
    public long id;

    /**
     * 表字段
     */
    public List<FieldNameDTO> field;
    
}
