package com.fisk.system.dto;

import lombok.Data;

/**
 * @author lishiji
 */
@Data
public class SqlCheckDTO {

    /**
     * sql语句
     */
    public String sql;

    /**
     * 数据库类型
     */
    public String dbType;

}
