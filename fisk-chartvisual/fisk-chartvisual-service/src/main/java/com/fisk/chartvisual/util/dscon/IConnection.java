package com.fisk.chartvisual.util.dscon;

/**
 * 数据源连接接口
 * @author gy
 */
public interface IConnection {

    /**
     * 连接
     */
    void connection();

    /**
     * 执行命令
     */
    void execCommand();
}
