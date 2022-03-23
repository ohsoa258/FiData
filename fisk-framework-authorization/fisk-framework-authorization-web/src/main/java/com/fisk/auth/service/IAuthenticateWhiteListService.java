package com.fisk.auth.service;

/**
 * @author gy
 */
public interface IAuthenticateWhiteListService {

    /**
     * 加载数据到redis
     * @param path 请求路径
     * @return 是否在白名单
     */
    boolean loadDataToRedis(String path);

    /**
     * 加载数据到redis
     * @param path 请求路径
     * @return 是否在白名单
     */
    boolean pathIsExists(String path);
}
