package com.fisk.auth.constants;

/**
 * @author: Lock
 * @data: 2021/5/17 13:55
 *
 * 定义cookie的属性
 */
public class JwtConstants {

    /**
     * 用户token的cookie名称
     */
    public static final String COOKIE_NAME = "FK_TOKEN";
    /**
     * 用户token的cookie的domain属性,决定cookie在哪些域名下生效,
     * 即fisk.com下的所有二级以上域名共享cookie
     */
    public static final String DOMAIN = "fisk.com";

}
