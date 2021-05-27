package com.fisk.chartvisual.service;

/**
 * 使用数据库
 * @author gy
 */
public interface IUseDataBase {

    /**
     * 测试数据库连接
     * @return 是否成功连接
     */
    boolean testConnection(int id);

}
