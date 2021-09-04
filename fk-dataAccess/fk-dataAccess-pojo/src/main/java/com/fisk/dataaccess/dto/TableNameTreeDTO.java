package com.fisk.dataaccess.dto;

import lombok.Data;

/**
 * @author Lock
 */
@Data
public class TableNameTreeDTO {

    /**
     * id
     */
    public long id;
    /**
     * 父id
     */
    public long pid;
    /**
     * 同步方式
     */
    public String syncMode;
    /**
     * 表达式
     */
    public String expression;
    /**
     * 日志
     */
    public String msg;
    /**
     * 物理表名
     */
    public String tableName;
    /**
     * 1: 数据接入; 2:数据建模
     */
    public int flag;
}
