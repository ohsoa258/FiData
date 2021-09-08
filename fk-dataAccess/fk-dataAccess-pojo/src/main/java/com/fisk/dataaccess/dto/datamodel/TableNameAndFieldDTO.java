package com.fisk.dataaccess.dto.datamodel;

import lombok.Data;

import java.util.List;

/**
 * @author Lock
 */
@Data
public class TableNameAndFieldDTO {

    public String tableName;

    /**
     * 返回给前端的唯一标记
     */
    public int tag;

    /**
     * 表字段
     */
    public List<String> fields;
}
