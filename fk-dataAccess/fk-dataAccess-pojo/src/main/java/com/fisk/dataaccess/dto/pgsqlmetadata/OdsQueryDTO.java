package com.fisk.dataaccess.dto.pgsqlmetadata;

import lombok.Data;

/**
 * @author JianWenYang
 */
@Data
public class OdsQueryDTO {
    public int pageIndex;
    public int pageSize;
    public int total;
    /**
     * 查询SQL语句
     */
    public String querySql;
}
