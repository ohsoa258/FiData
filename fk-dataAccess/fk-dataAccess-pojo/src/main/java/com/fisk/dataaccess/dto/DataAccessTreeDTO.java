package com.fisk.dataaccess.dto;

import lombok.Data;

import java.util.List;

/**
 * <p>
 *     应用注册树
 * </p>
 * @author Lock
 */
@Data
public class DataAccessTreeDTO {

    /**
     * id
     */
    public long id;
    /**
     * 应用名称
     */
    public String appName;
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
     * 应用注册下的物理表
     */
    public List<TableNameTreeDTO> list;

    /**
     * 1: 数据接入; 2:数据建模
     */
    public int flag;
}
