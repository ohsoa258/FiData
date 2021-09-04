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
    public static final String URL = "jdbc:mysql://192.168.11.130:3306/dmp_dataservice_db"+"?useUnicode=true&characterEncoding=utf-8&useSSL=false";

    /**
     * 3.用户名
     */
    public static final String USER = "root";

    /**
     * 4.密码
     */
    public static final String PASSWORD = "root123";
}
