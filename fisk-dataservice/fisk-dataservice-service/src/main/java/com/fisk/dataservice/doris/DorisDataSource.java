package com.fisk.dataservice.doris;

/**
 * @author WangYan
 * @date 2021/9/4 19:08
 */
public class DorisDataSource {
    /**
     *  1.反射加载Driver
     */
    public static final String DRIVER = "com.mysql.jdbc.Driver";

    /**
     * 2.创建连接
     */
    public static final String URL = "jdbc:mysql://192.168.11.134:9030/dmp_olap"+
            "?useUnicode=true&characterEncoding=utf8&allowMultiQueries=true&useSSL=false";

    /**
     * 3.用户名
     */
    public static final String USER = "root";

    /**
     * 4.密码
     */
    public static final String PASSWORD = "Password01!";
}
